package server.ban;

import database.ban.HwidBan;
import database.ban.HwidBanRepository;
import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.ThreadSafe;
import net.server.coordinator.session.Hwid;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Ponk
 */
@ThreadSafe
@Slf4j
public class HwidBanManager {
    private final HwidBanRepository hwidBanRepository;
    private final Set<Hwid> bannedHwids = new HashSet<>();

    public HwidBanManager(HwidBanRepository hwidBanRepository) {
        this.hwidBanRepository = hwidBanRepository;
    }

    public synchronized void loadHwidBans() {
        List<HwidBan> hwidBans = hwidBanRepository.getAllHwidBans();
        log.debug("Loaded {} hwid bans", hwidBans.size());
        bannedHwids.addAll(hwidBans.stream()
                .map(HwidBanManager::createHwid)
                .filter(Objects::nonNull)
                .toList()
        );
    }

    private static Hwid createHwid(HwidBan hwidBan) {
        try {
            return new Hwid(hwidBan.hwid());
        } catch (IllegalArgumentException e) {
            log.warn("Unable to create Hwid from: {} due to bad 'hwid' value in database", hwidBan);
            return null;
        }
    }

    public synchronized boolean isBanned(Hwid hwid) {
        return bannedHwids.contains(hwid);
    }

    public synchronized void banHwid(Hwid hwid, int accountId) {
        if (hwid == null) {
            throw new IllegalArgumentException("hwid cannot be null");
        }
        hwidBanRepository.saveHwidBan(hwid.hwid(), accountId);
    }
}
