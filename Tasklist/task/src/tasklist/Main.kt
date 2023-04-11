package tasklist

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.io.File
import java.util.*

var taskPriorityMap = mapOf(
    "C" to "\u001B[101m \u001B[0m", // Red
    "H" to "\u001B[103m \u001B[0m", // Yelllow
    "N" to "\u001B[102m \u001B[0m", // Green
    "L" to "\u001B[104m \u001B[0m", // Blue
)

var taskTimeTegMap = mapOf(
    "I" to "\u001B[102m \u001B[0m", // Green
    "T" to "\u001B[103m \u001B[0m", // Yellow
    "O" to "\u001B[101m \u001B[0m", // Red
)

data class Task(
    var taskPriority: String? = null,
    var taskDate: String? = null,
    var taskTime: String? = null,
    var taskStrings: MutableList<String>? = null
)

fun strToDate(data: String?): LocalDate {
    var tD = data.toString().split("-").map { it.toInt() }
    return LocalDate(tD[0], tD[1], tD[2])
}

fun strToTime(data: String): LocalTime {
    var tT = data.toString().split(":").map { it.toInt() }
    return LocalTime(tT[0], tT[1])
}

fun dateToStr(data: LocalDate) = data.toString()

fun timeToStr(data: LocalTime) = data.toString()

fun addtaskPriority(task: Task) {
    var tP = ""
    do {
        println("Input the task priority (C, H, N, L):")
        tP = readln().uppercase().trim()
    } while ((tP !in "CHNL".uppercase()) or (tP.isEmpty()))
    task.taskPriority = taskPriorityMap[tP.uppercase()]!!
}

fun addtaskDate(task: Task) {
    do {
        println("Input the date (yyyy-mm-dd):")
        var dateString = readln()
        try {
            task.taskDate = dateToStr(strToDate(dateString))
            break
        } catch (e: Exception) {
            println("The input date is invalid")
        }
    } while (true)
}

fun addtaskTime(task: Task) {
    do {
        println("Input the time (hh:mm):")
        var timeString = readln()
        try {
            task.taskTime = timeToStr(strToTime(timeString))

            break
        } catch (e: Exception) {
            println("The input time is invalid")
        }
    } while (true)
}

fun addTaskStrings(task: Task) {
    var strings = MutableList(0) { "" }
    task.taskStrings?.clear()
    println("Input a new task (enter a blank line to end):")
    val scan = Scanner(System.`in`)
    while (scan.hasNextLine()) {
        var str = scan.nextLine().trim()
        if (str != "") {
            strings.add(str)
        } else break
    }
    if (strings.size > 0) {
        strings.forEach {
            task.taskStrings?.add(it)
        }
        strings.clear()
    } else {
        println("The task is blank")
    }
}

fun main() {
    val NoTasksMessage = "No tasks have been input"
    var taskTimeTeg: String = "TimeTeg"
    var tasks = mutableListOf<Task>()
    val gson = Gson()
    val tasksType = object : TypeToken<MutableList<Task>>() {}.type
    var jsonString: String? = null
    var jsonFile = File("tasklist.json")

    if (File("tasklist.json").exists()) {
        jsonString = jsonFile.readText()
        tasks = gson.fromJson(jsonString, tasksType)
    }

    fun splitStrToEqualsParts(inputString: String, partLength: Int, addSpacesToLastStr: Boolean): List<String> {
        var stringsList = mutableListOf<String>()
        var partsCount = inputString.length / partLength
        if (inputString.length.toFloat() % partLength.toFloat() > 0) partsCount += 1
        for (i in 1..partsCount - 1) stringsList.add(inputString.substring((i - 1) * partLength..(i - 1) * partLength + partLength - 1))
        stringsList.add(inputString.substring((partsCount - 1) * partLength))
        if (addSpacesToLastStr) {
            stringsList[stringsList.lastIndex] = stringsList[stringsList.lastIndex].padEnd(partLength)
        }
        return stringsList
    }

    fun printTaskList() {
        val TASK_STR_LENGTH = 44
        val STR_SEPAROTOR = "+----+------------+-------+---+---+--------------------------------------------+"
        var spaces = ""
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
        println(
            STR_SEPAROTOR + "\n| N  |    Date    | Time  | P | D |                   Task                     |\n"
                    + STR_SEPAROTOR
        )
        for (i in 0..tasks.size - 1) {
            val taskDate = strToDate(tasks[i].taskDate)
            val numberOfDays = currentDate.daysUntil(taskDate!!)
            var strings = mutableListOf<String>()
            when {
                numberOfDays == 0 -> taskTimeTeg = taskTimeTegMap["T"].toString()

                numberOfDays < 0 -> taskTimeTeg = taskTimeTegMap["O"].toString()

                numberOfDays > 0 -> taskTimeTeg = taskTimeTegMap["I"].toString()
            }
            if (i < 9) spaces = "  "
            else spaces = " "
            for (taskString in tasks[i].taskStrings!!) {
                splitStrToEqualsParts(taskString, TASK_STR_LENGTH, true).forEach {
                    strings.add(it)
                }
            }
            println("| ${i + 1}$spaces| ${tasks[i].taskDate} | ${tasks[i].taskTime} | ${tasks[i].taskPriority} | ${taskTimeTeg} |${strings[0]}|")
            for (k in 1..strings.lastIndex) {
                println("|    |            |       |   |   |${strings[k]}|")
            }
            println(STR_SEPAROTOR)
        }
    }

    fun editTask() {
        var i: Int? = null
        printTaskList()
        do {
            println("Input the task number (1-${tasks.lastIndex + 1}):")
            var s = readln()
            try {
                i = s.toInt() - 1
                if (i in 0..tasks.lastIndex) {
                    do {
                        println("Input a field to edit (priority, date, time, task):")
                        when (readln()) {
                            "priority" -> addtaskPriority(tasks[i])
                            "date" -> addtaskDate(tasks[i])
                            "time" -> addtaskTime(tasks[i])
                            "task" -> addTaskStrings(tasks[i])
                            else -> {
                                println("Invalid field")
                                continue
                            }
                        }
                        println("The task is changed")
                        break
                    } while (true)
                } else {
                    println("Invalid task number")
                    continue
                }
            } catch (e: Exception) {
                println("Invalid task number")
                continue
            }
        } while (i !in 0..tasks.lastIndex)
    }

    fun deleteTask() {
        var i: Int? = null
        printTaskList()
        do {
            println("Input the task number (1-${tasks.lastIndex + 1}):")
            var s = readln()
            try {
                i = s.toInt() - 1
                if (i in 0..tasks.lastIndex) {
                    tasks.removeAt(i!!)
                    println("The task is deleted")
                    break
                } else {
                    println("Invalid task number")
                    continue
                }
            } catch (e: Exception) {
                println("Invalid task number")
                continue
            }
        } while (true)
    }
    while (true) {
        println("Input an action (add, print, edit, delete, end):")
        var inp = readln()
        when (inp.lowercase()) {
            "add" -> {
                var task = Task("", "", "", mutableListOf())
                addtaskPriority(task)
                addtaskDate(task)
                addtaskTime(task)
                addTaskStrings(task)
                tasks.add(task)
            }

            "print" -> {
                if (tasks.isEmpty()) println(NoTasksMessage)
                else printTaskList()
            }

            "edit" -> {
                if (tasks.isEmpty()) println(NoTasksMessage)
                else editTask()
            }

            "delete" -> {
                if (tasks.isEmpty()) println(NoTasksMessage)
                else deleteTask()
            }

            "end" -> {
                println("Tasklist exiting!")
                jsonString = gson.toJson(tasks, tasksType)
                jsonFile.writeText(jsonString)
                break
            }

            else -> println("The input action is invalid")
        }
    }
}