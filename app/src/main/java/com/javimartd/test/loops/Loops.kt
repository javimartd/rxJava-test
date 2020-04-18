package com.javimartd.test.loops

/**
 * https://kotlinlang.org/docs/reference/control-flow.html#for-loops
 */
object Loops {

    fun main(names: Array<String>) {

        for(name in names) println(name)

        for(index:Int in names.indices) println(names[index])

        for((index, value) in names.withIndex()) println("The value at index $index is $value")

        names.forEach { println(it) }

        names.forEachIndexed { index, value ->
            println("The value at index $index is $value")
        }

        for (index in 0..10) println(index)
    }
}