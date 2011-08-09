/*
 SCR:CommandId:list
 SCR:Description:Lists commands
 SCR:Help: 
 */

def scripts = commandManager.getAll().sort { it.getId() };
println "Available commands:"
scripts.each {
    //println "\t"+ it.getId() + "\t\t" + it.getDescription();
    console.writeLine "\t%1\$-25s %2\$-35s", it.getId(), it.getDescription()
}