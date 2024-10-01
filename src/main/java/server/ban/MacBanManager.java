package server.ban;

import database.ban.MacBan;
import database.ban.MacBanRepository;
import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.ThreadSafe;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ponk
 */
@ThreadSafe
@Slf4j
public class MacBanManager {
    private final MacBanRepository macBanRepository;
    private final Set<String> bannedMacs = new HashSet<>();

    public MacBanManager(MacBanRepository macBanRepository) {
        this.macBanRepository = macBanRepository;
    }

    public synchronized void loadMacBans() {
        List<MacBan> macBans = macBanRepository.getAllMacBans();
        log.debug("Loaded {} mac bans", macBans.size());
        bannedMacs.addAll(macBans.stream().map(MacBan::mac).toList());
    }

    public synchronized boolean isBanned(String mac) {
        return bannedMacs.contains(mac);
    }

    public synchronized void banMac(String mac, int accountId) {
        if (mac == null) {
            throw new IllegalArgumentException("mac cannot be null");
        }
        // TODO: validate mac format. Or create "Mac" model class.

        bannedMacs.add(mac);
        macBanRepository.saveMacBan(accountId, mac);
    }
}
