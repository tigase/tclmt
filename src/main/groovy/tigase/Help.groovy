/*
 * Tigase XMPP Server Command Line Management Tool
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */

/*
 SCR:CommandId:help
 SCR:Description:Display help
 SCR:Help: 
 */

if (args == null || args.length == 0) {
    console.writeLine "Usage:";
    console.writeLine "\ttclmt <-debug> <-u username> <-p password> <-s server> <-ip server_ip> <-i>"
    console.writeLine "\t\t-i\t-\tinteractive mode"
    console.writeLine "";
    return commandManager.executeScript("list", bindings);
}

def scripts = commandManager.getAll().sort { it.getId() };
scripts.findAll { it -> it.getId() == args[0]}.each {
    //println "\t"+ it.getId() + "\t\t" + it.getDescription();
    console.writeLine "\t%s %2\$-35s", it.getId(), it.getHelp()
}