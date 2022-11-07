import java.lang.NumberFormatException
import java.lang.StringBuilder
import java.util.Scanner

val sets = HashMap<String, HashMap<String, Double>>()

fun main() {
    val scanner = Scanner(System.`in`)

    println("Enter Ctrl+D to exit the program")

    while (true) {
        print("> ")
        if (!scanner.hasNextLine()) break

        var command = scanner.nextLine().toCommand()

        if (command.isEmpty()) continue

        when (command.getCurrent()) {
            "help" -> println("NEED TO UPDATE")
            "add" -> addCommand(command.getNext())
            "get" -> getCommand(command.getNext())
            else -> unknownCommandMessage(command.getCurrent())
        }
    }
}

fun addCommand(command: String) {
    if (command.isEmpty()) errorMessage("nothing to add").also { return }

    when (command.getCurrent()) {
        "set" -> addSetCommand(command.getNext().replaceAll(" ", ""))
        else -> unknownCommandMessage(command)
    }
}

fun addSetCommand(setData: String) {
    if (setData.isEmpty()) errorMessage("set data not entered").also { return }

    if (!(setData.contains('=') && setData.contains('{') && setData.contains('}')
                && (setData.indexOf('=') < setData.indexOf('{'))
                && (setData.indexOf('{') < setData.indexOf('}'))))
        errorMessage("wrong set structure$setData\nRight structure (n - number of set elements):\n" +
                "<set_name> = {<probability_1>/<probability_name>, ..., <probability_n>/<probability_n>}")
            .also { return }

    val setName = setData.getCurrent('=')
    if (!isRightName(setName, "set name")) return
    if (setName in sets.keys) errorMessage("set name \"$setName\" already exists").also { return }

    val set = HashMap<String, Double>()
    for (element in setData.getNext('=').replace("{", "").replace("}", "").split(',')) {
        if (!element.contains('/')) errorMessage("wrong set element \"$element\"" +
                "Right set element structure (i in range from 0 to n - number of set elements):" +
                "<probability_i>/<probability_i>")
            .also { return }

        val elementValue: Double
        try {
            elementValue = element.getCurrent('/').toDouble()
        } catch (e: NumberFormatException) {
            errorMessage("wrong element value \"${element.getCurrent('/')}\"\nOnly decimal digits allowed")
            return
        }

        val elementName = element.getNext('/')
        if (!isRightName(elementName, "element name")) return
        if (elementName in set.keys) errorMessage("element name \"$elementName\" already exists").also { return }

        set[elementName] = elementValue
    }

    sets[setName] = set
}

fun getCommand(command: String) {
    if (command.isEmpty()) errorMessage("nothing to get").also { return }

    when (command.getCurrent()) {
        "set" -> getSetCommand(command.getNext())
        "sets" -> if (command.getNext().isNotEmpty()) unknownCommandMessage(command.getNext()) else getSetsCommand()
        else -> unknownCommandMessage(command)
    }
}

fun getSetCommand(setName: String) {
    if (setName in sets.keys) {
        print("$setName = {")
        for (elementKey in sets[setName]!!.keys) print("$elementKey/${sets[setName]!![elementKey]}, ")
        println("\b\b}")
    }
    else errorMessage("set \"$setName\" not found")
}

fun getSetsCommand() {
    for (setKey in sets.keys) {
        print("$setKey = {")
        for (elementKey in sets[setKey]!!.keys) print("$elementKey/${sets[setKey]!![elementKey]}, ")
        println("\b\b}")
    }
}

fun String.toCommand(): String {
    val builder = StringBuilder()
    var flag = false

    for (symbol in this) {
        if (symbol == ' ') {
            if (flag) {
                builder.append(symbol)
                flag = false
            }
        } else {
            builder.append(symbol.lowercase())
            flag = true
        }
    }

    return builder.toString()
}

fun String.getCurrent(divider: Char = ' '): String = if (this.contains(divider)) this.substring(0, this.indexOf(divider)) else this

fun String.getNext(divider: Char = ' '): String = if (this.contains(divider)) this.substring(this.indexOf(divider) + 1, this.length) else ""

fun String.replaceAll(oldValue: String, newValue: String): String {
    val builder = StringBuilder()

    var index = 0
    while (index <= this.length - oldValue.length) {
        if (this.substring(index, index + oldValue.length) != oldValue) {
            builder.append(this[index])
            index++
        }
        else {
            builder.append(newValue)
            index += oldValue.length
        }
    }
    while (index < this.length) {
        builder.append(this[index])
        index++
    }

    return builder.toString()
}

fun String.replaceAll(oldValue: Char, newValue: Char): String {
    val builder = StringBuilder()

    for (symbol in this)
        builder.append(if (symbol == oldValue) newValue else this)

    return builder.toString()
}

fun unknownCommandMessage(command: String) = errorMessage("unknown command \"${command}\"")

fun errorMessage(message: String) = println("Error: $message")

fun isRightName(name: String, nameType: String) : Boolean {
    if (name.isEmpty()) errorMessage("$nameType not entered").also { return false }

    for (symbol in name)
        if (symbol !in 'a'..'z' && symbol !in '0'..'9')
            errorMessage("wrong $nameType \"$name\"\nOnly letters and numbers allowed").also { return false }

    if (name[0] in '0'..'9') errorMessage("wrong $nameType \"$name\"\nIt cannot start with a number").also { return false }

    return true
}