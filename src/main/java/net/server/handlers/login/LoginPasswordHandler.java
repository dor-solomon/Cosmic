/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.server.handlers.login;

import client.Character;
import client.Client;
import client.DefaultDates;
import config.YamlConfig;
import constants.game.GameConstants;
import net.PacketHandler;
import net.packet.InPacket;
import net.server.Server;
import net.server.coordinator.session.Hwid;
import net.server.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AccountService;
import service.TransitionService;
import tools.BCrypt;
import tools.DatabaseConnection;
import tools.HexTool;
import tools.PacketCreator;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

public final class LoginPasswordHandler implements PacketHandler {
    private static final Logger log = LoggerFactory.getLogger(LoginPasswordHandler.class);

    private final AccountService accountService;
    private final TransitionService transitionService;

    public LoginPasswordHandler(AccountService accountService, TransitionService transitionService) {
        this.accountService = accountService;
        this.transitionService = transitionService;
    }

    @Override
    public boolean validateState(Client c) {
        return !c.isLoggedIn();
    }

    @Override
    public final void handlePacket(InPacket p, Client c) {
        String remoteHost = c.getRemoteAddress();
        if (remoteHost.contentEquals("null")) {
            c.sendPacket(PacketCreator.getLoginFailed(14));          // thanks Alchemist for noting remoteHost could be null
            return;
        }

        String login = p.readString();
        String pwd = p.readString();
        c.setAccountName(login);

        p.skip(6);   // localhost masked the initial part with zeroes...
        byte[] hwidNibbles = p.readBytes(4);
        Hwid hwid = new Hwid(HexTool.toCompactHexString(hwidNibbles));
        int loginok = c.login(login, pwd, hwid);


        if (YamlConfig.config.server.AUTOMATIC_REGISTER && loginok == 5) {
            try {
                int accountId = createAccountPostgres(login, pwd);
                createAccountMysql(accountId, login, pwd);
                c.setAccID(accountId);
            } finally {
                loginok = c.login(login, pwd, hwid);
            }
        }

        if (c.hasBannedIP() || c.hasBannedMac()) {
            c.sendPacket(PacketCreator.getLoginFailed(3));
            return;
        }
        Calendar tempban = c.getTempBanCalendarFromDB();
        if (tempban != null) {
            if (tempban.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()) {
                c.sendPacket(PacketCreator.getTempBan(tempban.getTimeInMillis(), c.getGReason()));
                return;
            }
        }
        if (loginok == 3) {
            c.sendPacket(PacketCreator.getPermBan(c.getGReason()));//crashes but idc :D
            return;
        } else if (loginok != 0) {
            c.sendPacket(PacketCreator.getLoginFailed(loginok));
            return;
        }
        if (c.finishLogin()) {
            checkChar(c);
            login(c);
        } else {
            c.sendPacket(PacketCreator.getLoginFailed(7));
        }
    }

    private int createAccountPostgres(String name, String password) {
        return accountService.createNew(name, password);
    }

    private void createAccountMysql(int id, String name, String password) {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO accounts (id, name, password, birthday, tempban) VALUES (?, ?, ?, ?, ?);")) {
            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setString(3, BCrypt.hashpw(password, BCrypt.gensalt(12)));
            ps.setDate(4, Date.valueOf(DefaultDates.getBirthday()));
            ps.setTimestamp(5, Timestamp.valueOf(DefaultDates.getTempban()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkChar(Client c) { // issue with multiple chars from same account login found by shavit, resinate
        if (!YamlConfig.config.server.USE_CHARACTER_ACCOUNT_CHECK) {
            return;
        }

        final int accId = c.getAccID();
        for (World w : Server.getInstance().getWorlds()) {
            for (Character chr : w.getPlayerStorage().getAllCharacters()) {
                if (accId == chr.getAccountID()) {
                    log.warn("Chr {} has been removed from world {}. Possible Dupe attempt.", chr.getName(), GameConstants.WORLD_NAMES[w.getId()]);
                    transitionService.forceDisconnect(c);
                    w.getPlayerStorage().removePlayer(chr.getId());
                }
            }
        }
    }

    private static void login(Client c) {
        c.sendPacket(PacketCreator.getAuthSuccess(c));//why the fk did I do c.getAccountName()?
        Server.getInstance().registerLoginState(c);
    }
}
