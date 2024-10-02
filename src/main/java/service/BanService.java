package service;

import client.Character;
import client.Client;
import client.autoban.AutobanFactory;
import config.YamlConfig;
import lombok.extern.slf4j.Slf4j;
import net.packet.Packet;
import net.server.Server;
import net.server.coordinator.session.Hwid;
import server.TimerManager;
import server.ban.HwidBanManager;
import server.ban.IpBanManager;
import server.ban.MacBanManager;
import tools.PacketCreator;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BanService {
    private final AccountService accountService;
    private final TransitionService transitionService;
    private final IpBanManager ipBanManager;
    private final MacBanManager macBanManager;
    private final HwidBanManager hwidBanManager;

    public BanService(AccountService accountService, TransitionService transitionService, IpBanManager ipBanManager,
                      MacBanManager macBanManager, HwidBanManager hwidBanManager) {
        this.accountService = accountService;
        this.transitionService = transitionService;
        this.ipBanManager = ipBanManager;
        this.macBanManager = macBanManager;
        this.hwidBanManager = hwidBanManager;
    }

    public void autoban(Character chr, AutobanFactory type, String reason) {
        autoban(chr, "Autobanned for (" + type.name() + ": " + reason + ")");
    }

    private void autoban(Character chr, String reason) {
        if (isExempt(chr)) {
            return;
        }

        chr.setBanned();
        accountService.ban(chr.getAccountID(), null, (byte) 0, reason);

        chr.sendPacket(PacketCreator.sendPolice("You have been blocked by the#b %s Police for HACK reason.#k".formatted("Cosmic")));
        TimerManager.getInstance().schedule(() -> transitionService.disconnect(chr.getClient(), false),
                TimeUnit.SECONDS.toMillis(5));

        var bannedName = Character.makeMapleReadable(chr.getName());
        Packet autobanGmNotice = PacketCreator.serverNotice(6, bannedName + " was autobanned for " + reason);
        Server.getInstance().broadcastGMMessage(chr.getWorld(), autobanGmNotice);
    }

    public void addPoint(Character chr, AutobanFactory type, String reason) {
        if (isExempt(chr)) {
            return;
        }

        var autobanManager = chr.getAutobanManager();
        boolean shouldAutoban = autobanManager.addPoint(type);
        if (shouldAutoban) {
            autoban(chr, type, reason);
        }

        if (YamlConfig.config.server.USE_AUTOBAN_LOG) {
            log.info("Autoban - chr {} caused {} {}", Character.makeMapleReadable(chr.getName()), type.name(), reason);
        }
    }

    private boolean isExempt(Character chr) {
        return !YamlConfig.config.server.USE_AUTOBAN || chr.isGM() || chr.isBanned();
    }

    public void permaBan(Client c, String victimName, byte reason, String description) {
        ban(c, victimName, null, reason, description);
    }

    public void tempBan(Client c, String victimName, Duration duration, byte reason, String description) {
        ban(c, victimName, duration, reason, description);
    }

    // TODO: also ban ip and macs. Table "ipbans" and "macbans" (while taking "macfilters" into consideration).
    // That's how it was done previously, anyway.
    private void ban(Client c, String victimName, Duration duration, byte reason, String description) {
        Character victim = c.getChannelServer().getPlayerStorage().getCharacterByName(victimName);

        boolean success;
        if (victim == null) {
            success = banOfflineChr(victimName, duration, reason, description);
        } else {
            success = banOnlineChr(c, victim, duration, reason, description);
        }

        if (!success) {
            c.sendPacket(PacketCreator.getGMEffect(6, (byte) 1));
            return;
        }
        c.sendPacket(PacketCreator.getGMEffect(4, (byte) 0));
        Server.getInstance().broadcastMessage(c.getWorld(), PacketCreator.serverNotice(6, "%s has been banned.".formatted(victimName)));
    }

    private boolean banOfflineChr(String victimName, Duration duration, byte reason, String description) {
        Optional<Integer> foundAccountId = accountService.getAccountIdByChrName(victimName);
        if (foundAccountId.isEmpty()) {
            return false;
        }

        saveBan(foundAccountId.get(), duration, reason, description);
        return true;
    }

    private boolean banOnlineChr(Client c, Character victim, Duration duration, byte reason, String description) {
        victim.setBanned();
        String readableName = Character.makeMapleReadable(victim.getName());
        String ip = victim.getClient().getRemoteAddress();
        String enrichedDescription = "[%s] %s (IP: %s)".formatted(description, readableName, ip);
        saveBan(victim.getAccountID(), duration, reason, enrichedDescription);
        victim.sendPacket(PacketCreator.sendPolice("You have been banned by %s.".formatted(c.getPlayer().getName())));
        TimerManager.getInstance().schedule(() -> transitionService.disconnect(c, false),
                TimeUnit.SECONDS.toMillis(5));
        return true;
    }

    private void saveBan(int accountId, Duration duration, byte reason, String description) {
        final Instant bannedUntil;
        if (duration != null) {
            bannedUntil = Instant.now().plus(duration);
        } else {
            bannedUntil = null;
        }
        accountService.ban(accountId, bannedUntil, reason, description);
    }

    public boolean isBanned(Client c) {
        return isIpBanned(c) || isHwidBanned(c) || isMacBanned(c);
    }

    private boolean isIpBanned(Client c) {
        String ip = c.getRemoteAddress();
        return ip != null && ipBanManager.isBanned(ip);
    }

    private boolean isHwidBanned(Client c) {
        Hwid hwid = c.getHwid();
        return hwid != null && hwidBanManager.isBanned(hwid);
    }

    private boolean isMacBanned(Client c) {
        Set<String> macs = c.getMacs();
        return macs.stream().anyMatch(macBanManager::isBanned);
    }
}
