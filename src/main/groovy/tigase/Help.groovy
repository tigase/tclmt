/*
 SCR:CommandId:help
 SCR:Description:Display help
 SCR:Help: 
 */

if (args == null || args.length == 0) {
    console.writeLine "Usage:";
    console.writeLine "\ttclmt <-u username> <-p password> <-s server> <-ip server_ip> <-i>"
    console.writeLine "\t\t-i\t-\tinteractive mode"
    console.writeLine "";
    return commandManager.executeScript("list", bindings);
}

def scripts = commandManager.getAll().sort { it.getId() };
scripts.findAll { it -> it.getId() == args[0]}.each {
    //println "\t"+ it.getId() + "\t\t" + it.getDescription();
    console.writeLine "\t%s %2\$-35s", it.getId(), it.getHelp()
}