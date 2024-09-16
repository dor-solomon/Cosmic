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

import client.Client;
import client.creator.JobType;
import client.creator.novice.BeginnerCreator;
import client.creator.novice.LegendCreator;
import client.creator.novice.NoblesseCreator;
import net.AbstractPacketHandler;
import net.packet.InPacket;
import tools.PacketCreator;

public final class CreateCharHandler extends AbstractPacketHandler {

    @Override
    public void handlePacket(InPacket p, Client c) {
        String name = p.readString();
        int job = p.readInt();
        int face = p.readInt();

        int hair = p.readInt();
        int haircolor = p.readInt();
        int skincolor = p.readInt();

        int top = p.readInt();
        int bottom = p.readInt();
        int shoes = p.readInt();
        int weapon = p.readInt();
        int gender = p.readByte();

        /*
        NewCharacterSpec spec = NewCharacterSpec.builder()
                .type(parseJobType(p.readInt()))
                .face(p.readInt())
                .hair(p.readInt())
                .hairColor(p.readInt())
                .skin(p.readInt())
                .topItemId(p.readInt())
                .bottomItemId(p.readInt())
                .shoesItemId(p.readInt())
                .weaponItemId(p.readInt())
                .gender(p.readByte())
                .build();
         */

        int status;
        switch (job) {
        case 0: // Knights of Cygnus
            status = NoblesseCreator.createCharacter(c, name, face, hair + haircolor, skincolor, top, bottom, shoes, weapon, gender);
            break;
        case 1: // Adventurer
            status = BeginnerCreator.createCharacter(c, name, face, hair + haircolor, skincolor, top, bottom, shoes, weapon, gender);
            break;
        case 2: // Aran
            status = LegendCreator.createCharacter(c, name, face, hair + haircolor, skincolor, top, bottom, shoes, weapon, gender);
            break;
        default:
            c.sendPacket(PacketCreator.deleteCharResponse(0, 9));
            return;
        }

        if (status == -2) {
            c.sendPacket(PacketCreator.deleteCharResponse(0, 9));
        }
    }

    private static JobType parseJobType(int value) {
        return switch (value) {
            case 0 -> JobType.KNIGHT_OF_CYGNUS;
            case 1 -> JobType.ADVENTURER;
            case 2 -> JobType.ARAN;
            default -> throw new IllegalArgumentException("Invalid job type: " + value);
        };
    }
}
