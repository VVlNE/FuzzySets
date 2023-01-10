import org.jetbrains.letsPlot.export.ggsave
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleXContinuous
import org.jetbrains.letsPlot.scale.scaleYContinuous
import java.lang.NumberFormatException
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.HashMap
import java.util.SortedMap

val sets = HashMap<String, SortedMap<String, Double>>()
val matrices = HashMap<String, HashMap<String, HashMap<String, Double>>>()
val BANNED_NAMES = setOf("add", "get", "remove", "help", "all", "not")
val SET_OPERATORS = setOf("+", "*", "=", "->")
val MATRIX_OPERATORS = setOf("o")

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
            "add" -> addSetCommand(command.getNext().replaceAll(" ", ""))
            "get" -> getCommand(command.getNext())
            "remove" -> removeCommand(command.getNext())
            "not" -> makeInversionOfSet(command.getNext())
            "build" -> buildGraph(command.getNext())
            in sets.keys -> makeSetOperation(command.split(" "))
            in matrices.keys -> makeMatrixOperation(command.split(" "))
            else -> unknownCommandMessage(command.getCurrent())
        }
    }
}

fun addSetCommand(setData: String) {
    if (setData.isEmpty()) errorMessage("nothing to add").also { return }

    if (!(setData.contains('=') && setData.contains('{') && setData.contains('}')
                && (setData.indexOf('=') < setData.indexOf('{'))
                && (setData.indexOf('{') < setData.indexOf('}')))
    )
        errorMessage(
            "wrong set structure$setData\nRight structure (n - number of set elements):\n" +
                    "<set_name> = {<probability_1>/<probability_name>, ..., <probability_n>/<probability_n>}"
        )
            .also { return }

    val setName = setData.getCurrent('=')
    if (!isRightName(setName, "set name")) return
    if (setName in sets.keys) errorMessage("set name \"$setName\" already exists").also { return }

    val set = HashMap<String, Double>()
    for (element in setData.getNext('=').replace("{", "").replace("}", "").split(',')) {
        if (!element.contains('/')) errorMessage(
            "wrong set element \"$element\"" +
                    "Right set element structure (i in range from 0 to n - number of set elements):" +
                    "<probability_i>/<probability_i>"
        )
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

    sets[setName] = set.toSortedMap()
}

fun getCommand(name: String) {
    if (name.isEmpty()) errorMessage("nothing to get").also { return }

    when (name.getCurrent()) {
        in sets.keys -> {
            print("$name = {")
            for (key in sets[name]!!.keys) print("${String.format("%.4f", sets[name]!![key])}/$key, ")
            println("\b\b}")
        }

        in matrices.keys -> {
            println("$name")
            var flag = true
            for (row in matrices[name]!!.keys) {
                if (flag) {
                    for (key in matrices[name]!![row]!!.keys) print("\t$key\t")
                    print("\b")
                    flag = false
                }
                print("\n$row")
                for (col in matrices[name]!![row]!!.keys)
                    print("\t${String.format("%.4f", matrices[name]!![row]!![col]).replace(',', '.')}")
            }
            println()
        }

        "all" -> if (name.getNext().isNotEmpty()) unknownCommandMessage(name.getNext()) else {
            for (key in sets.keys) getCommand(key)
            for (key in matrices.keys) getCommand(key)
        }

        else -> errorMessage("\"$name\" not found")
    }
}

fun removeCommand(name: String) {
    if (name.isEmpty()) errorMessage("nothing to remove").also { return }

    if (name !in sets.keys && name !in matrices.keys) errorMessage("\"$name\" not found").also { return }

    if (name in sets.keys) sets.remove(name)
    else matrices.remove(name)
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

fun String.getCurrent(divider: Char = ' '): String =
    if (this.contains(divider)) this.substring(0, this.indexOf(divider)) else this

fun String.getNext(divider: Char = ' '): String =
    if (this.contains(divider)) this.substring(this.indexOf(divider) + 1, this.length) else ""

fun String.replaceAll(oldValue: String, newValue: String): String {
    val builder = StringBuilder()

    var index = 0
    while (index <= this.length - oldValue.length) {
        if (this.substring(index, index + oldValue.length) != oldValue) {
            builder.append(this[index])
            index++
        } else {
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

fun isRightName(name: String, nameType: String): Boolean {
    if (name.isEmpty()) errorMessage("$nameType not entered").also { return false }

    if (name in BANNED_NAMES) errorMessage("name \"$name\" is prohibited").also { return false }

    for (symbol in name)
        if (symbol !in 'a'..'z' && symbol !in '0'..'9' && symbol != '_')
            errorMessage("wrong $nameType \"$name\"\nOnly letters, numbers and \"_\" are allowed").also { return false }

    if (name[0] in '0'..'9') errorMessage("wrong $nameType \"$name\"\nIt cannot start with a number").also { return false }

    return true
}

fun makeSetOperation(data: List<String>) {
    if (data.size != 3)
        errorMessage("wrong operation structure\nRight structure: <set_name_1> <operator> <set_name_2> ")
            .also { return }

    if (data[1] !in SET_OPERATORS) errorMessage("unknown operator \"${data[1]}\"").also { return }

    if (data[2] !in sets.keys) errorMessage("unknown set \"${data[2]}\"").also { return }

    when (data[1]) {
        "+" -> makeUnionOfSets(data[0], data[2])
        "*" -> makeIntersectionOfSets(data[0], data[2])
        "=" -> println(areEqual(data[0], data[2]))
        "->" -> makeProductionOfSets(data[0], data[2])
    }
}

fun makeUnionOfSets(name1: String, name2: String) {
    if ("$name1+$name2" in sets.keys) getCommand("$name1+$name2").also { return }
    if ("$name2+$name1" in sets.keys) getCommand("$name2+$name1").also { return }

    val set = HashMap<String, Double>()
    for (key in sets[name1]!!.keys) {
        if (key in sets[name2]!!.keys) {
            if (sets[name1]!![key]!! > sets[name2]!![key]!!) set[key] = sets[name1]!![key]!!
            else set[key] = sets[name2]!![key]!!
        } else set[key] = sets[name1]!![key]!!
    }

    for (key in sets[name2]!!.keys) {
        if (key in sets[name1]!!.keys) {
            if (sets[name2]!![key]!! > sets[name1]!![key]!!) set[key] = sets[name2]!![key]!!
            else set[key] = sets[name1]!![key]!!
        } else set[key] = sets[name2]!![key]!!
    }

    sets["$name1+$name2"] = set.toSortedMap()

    getCommand("$name1+$name2")
}

fun makeIntersectionOfSets(name1: String, name2: String) {
    if ("$name1*$name2" in sets.keys) getCommand("$name1*$name2").also { return }
    if ("$name2*$name1" in sets.keys) getCommand("$name2*$name1").also { return }

    val set = HashMap<String, Double>()
    for (key in sets[name1]!!.keys) {
        if (key in sets[name2]!!.keys) {
            if (sets[name1]!![key]!! < sets[name2]!![key]!!) set[key] = sets[name1]!![key]!!
            else set[key] = sets[name2]!![key]!!
        }
    }

    for (key in sets[name2]!!.keys) {
        if (key in sets[name1]!!.keys) {
            if (sets[name2]!![key]!! < sets[name1]!![key]!!) set[key] = sets[name2]!![key]!!
            else set[key] = sets[name1]!![key]!!
        }
    }

    sets["$name1*$name2"] = set.toSortedMap()

    getCommand("$name1*$name2")
}

fun areEqual(name1: String, name2: String): Boolean {
    for (key in sets[name1]!!.keys) {
        if (key in sets[name2]!!.keys) {
            if (sets[name1]!![key] != sets[name2]!![key]) return false
        } else {
            if (sets[name1]!![key] != 0.0) return false
        }
    }

    for (key in sets[name2]!!.keys) {
        if (key in sets[name1]!!.keys) {
            if (sets[name2]!![key] != sets[name1]!![key]) return false
        } else {
            if (sets[name2]!![key] != 0.0) return false
        }
    }

    return true
}

fun makeProductionOfSets(name1: String, name2: String) {
    if ("$name1->$name2" in matrices.keys) getCommand("$name1->$name2").also { return }

    val rows = HashMap<String, HashMap<String, Double>>()
    for (key1 in sets[name1]!!.keys) {
        val columns = HashMap<String, Double>()
        for (key2 in sets[name2]!!.keys) {
            if (sets[name1]!![key1]!! < sets[name2]!![key2]!!) columns[key2] = sets[name1]!![key1]!!
            else columns[key2] = sets[name2]!![key2]!!
        }
        rows[key1] = columns
    }

    matrices["$name1->$name2"] = rows

    getCommand("$name1->$name2")
}

fun makeInversionOfSet(name: String) {
    if (name.isEmpty()) errorMessage("nothing to inverse").also { return }

    if (name !in sets.keys && name !in matrices.keys) errorMessage("\"$name\" not found").also { return }

    var flag = true
    for (operator in SET_OPERATORS) if (operator in name) flag = false
    val inverseName = if (flag) "-$name" else "-($name)"

    if (name in sets.keys) {
        val set = HashMap<String, Double>()
        for (key in sets[name]!!.keys) set[key] = 1 - sets[name]!![key]!!
        sets[inverseName] = set.toSortedMap()
    } else {
        val rows = HashMap<String, HashMap<String, Double>>()
        for (row in matrices[name]!!.keys) {
            val columns = HashMap<String, Double>()
            for (column in matrices[name]!![row]!!.keys) columns[column] = 1 - matrices[name]!![row]!![column]!!
            rows[row] = columns
        }

        matrices[inverseName] = rows
    }

    getCommand(inverseName)
}

fun makeMatrixOperation(data: List<String>) {
    if (data.size != 3)
        errorMessage("wrong operation structure\nRight structure: <matrix_name_1> <operator> <matrix_name_2> ")
            .also { return }

    if (data[1] !in MATRIX_OPERATORS) errorMessage("unknown operator \"${data[1]}\"").also { return }

    if (data[2] !in matrices.keys) errorMessage("unknown matrix \"${data[2]}\"").also { return }

    when (data[1]) {
        "o" -> makeConvolutionOfMatrices(data[0], data[2])
    }
}

fun makeConvolutionOfMatrices(name1: String, name2: String) {
    if (matrices[name1]!![matrices[name1]!!.keys.random()]!!.size != matrices[name2]!!.size)
        errorMessage(
            "matrix multiplication is impossible.\n" +
                    "The number of columns of matrix $name1 is not equal to the number of rows of matrix $name2:\n" +
                    "${matrices[name1]!![matrices[name1]!!.keys.random()]!!.size} != ${matrices[name2]!!.size}"
        )
            .also { return }

    val name = "($name1)o($name2)"

    if (name in matrices.keys) getCommand(name).also { return }

    val transposedMatrix2 = HashMap<String, HashMap<String, Double>>()
    for (row in matrices[name2]!!.keys) {
        for (column in matrices[name2]!![row]!!.keys) {
            if (transposedMatrix2[column] == null) transposedMatrix2[column] = HashMap<String, Double>()
            transposedMatrix2[column]!![row] = matrices[name2]!![row]!![column]!!
        }
    }

    val rows = HashMap<String, HashMap<String, Double>>()
    for (row in matrices[name1]!!.keys) {
        val columns = HashMap<String, Double>()
        for (column in transposedMatrix2.keys) {
            columns[column] = 0.0
        }
        rows[row] = columns
    }

    for (row in matrices[name1]!!.keys) {
        for (column in transposedMatrix2.keys) {
            for ((i, j) in (matrices[name1]!![row]!!.keys).zip(transposedMatrix2[column]!!.keys)) {
                var value =
                    if (matrices[name1]!![row]!![i]!! < transposedMatrix2[column]!![j]!!) matrices[name1]!![row]!![i]!!
                    else transposedMatrix2[column]!![j]!!
                if (value > rows[row]!![column]!!) rows[row]!![column] = value
            }
        }
    }

    matrices[name] = rows

    getCommand(name)
}

fun buildGraph(name: String) {
    if (name !in matrices.keys) errorMessage("unknown matrix \"$name\"").also { return }

    //переписывание данных в нужный формат
    val data = mutableMapOf<String, MutableList<Double>>()
    val xValue = mutableListOf<Int>()
    val yValue = mutableListOf<Double>()
    val xNames = mutableListOf<String>()
    val yNames = mutableListOf<String>()

    for ((index, key) in matrices[name]!![matrices[name]!!.keys.random()]!!.keys.withIndex()) {
        xValue.add(index)
        xNames.add(key)
    }

    //добавление построения таблицы
    var p = letsPlot(data) { x = xValue }

    for ((index, row) in matrices[name]!!.keys.withIndex()) {
        data["y$index"] = mutableListOf()

        for (column in matrices[name]!![row]!!.keys)
            data["y$index"]!!.add(matrices[name]!![row]!![column]!!)

        val key = matrices[name]!![row]!!.keys.first()
        if (matrices[name]!![row]!![key]!! in yValue)
            yNames[yValue.indexOf(matrices[name]!![row]!![key]!!)] += ", $row"
        else {
            yNames.add(row)
            yValue.add(matrices[name]!![row]!![key]!!)
        }

        //TODO можно добавить цвета color = "#******"
        p += geomLine(alpha = 0.4, size = 2) { y = "y$index" }
    }

    for (i in 0..10)
        if ((i.toDouble() / 10) !in yValue) {
            yNames.add((i.toDouble() / 10).toString())
            yValue.add(i.toDouble() / 10)
        }

    p += scaleXContinuous("", breaks = xValue, labels = xNames)
    p += scaleYContinuous("", breaks = yValue, labels = yNames)

    ggsave(p, buildString {
        for (symbol in name) if (symbol in 'a'..'z' || symbol == '_' || symbol == '-') append(symbol)
        append(".jpeg")
    })
}

//комманды
//add Food = {0/f1, 0.33/f2, 0.67/f3, 1/f4, 0.84/f5, 0.68/f6, 0.52/f7}
//add Water = {0/w1, 1/w2, 0.6/w3}
//add Balance = {0/b1, 0.25/b2, 0.5/b3, 0.75/b4, 1/b5}
//Food -> Balance
//build Food->Balance
//not Water
//not Balance
//-Water -> Balance
//-Balance -> Water
//-Water->Balance o -Balance->Water
//build (-water->balance)o(-balance->water)