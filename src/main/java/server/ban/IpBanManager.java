package server.ban;

import database.ban.IpBan;
import database.ban.IpBanRepository;
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
public class IpBanManager {
    private final IpBanRepository ipBanRepository;
    private final Set<String> bannedIps = new HashSet<>();

    public IpBanManager(IpBanRepository ipBanRepository) {
        this.ipBanRepository = ipBanRepository;
    }

    public synchronized void loadIpBans() {
        List<IpBan> ipBans = ipBanRepository.getAllIpBans();
        log.debug("Loaded {} ip bans", ipBans.size());
        bannedIps.addAll(ipBans.stream().map(IpBan::ip).toList());
    }

    public synchronized boolean isBanned(String ip) {
        return bannedIps.contains(ip);
    }

    public synchronized void banIp(String ip, int accountId) {
        if (ip == null) {
            throw new IllegalArgumentException("ip cannot be null");
        }
        // TODO: validate ip format. Or create "Ip" model class.

        bannedIps.add(ip);
        ipBanRepository.saveIpBan(accountId, ip);
    }

}
