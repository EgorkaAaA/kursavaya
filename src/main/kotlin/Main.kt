import java.math.BigInteger
import java.util.*
import kotlin.math.E
import kotlin.math.log10
import kotlin.math.pow
import kotlin.streams.toList

// Начальные данные
val fixedCosts = 15000 // Постоянные затраты
val depreciation = 10000 // Амортизация
val taxProcent = 0.34 // Процент налога
val variableCostsProcent = 0.75 // Процент переменных затрат
val startCoast = 150000 // Начальная стоимость
val sellPrice = 35000 // Цена продажи
val coastOfCapital = 0.1 // Цена капитала
var years = 4 // Года

// Вычесляемые данные
var supply = 0 // Доход
var variableCosts = 0.0 //Переменные затраты
var profitBeforeTax = 0.0// Прибиль до налога
var tax = 0.0 // Налог
var profitAfterTax = 0.0 // Прибиль после налога

// TODO: не мин макс а среднее и отклонение ПЕРЕДЕЛАТЬ
fun main() {
    print("Года: ")
    years = readLine()!!.toInt()
    print("Средний спрос: ")
    val avgValue = readLine()!!.toInt()
    print("Максимальное отклонение: ")
    val criticalValue  = readLine()!!.toInt()
    //Генерация спроса
    val iteration = 1000
    val cpsList = mutableListOf<Double>()
    println("1. Равномерное распределение \n2. Паусовское распределение")
    val mod = readLine()!!.toInt()
    for (i in 1..iteration) {
        var clearProfit: MutableList<Double> // Чистая прибыль
        val demand = mutableListOf<Int>()  // Спрос


        for (i in 1..years) {
            if (mod == 1) {
                demand.add(randomDemand(avgValue,criticalValue))
            }
            if (mod == 2) {
                demand.add(cummulitiveDistributionFunction((avgValue+criticalValue) - (avgValue-criticalValue)))
            }
        }


        clearProfit = dataV(demand)
        val cpsV = cps(years, coastOfCapital, clearProfit) - startCoast
        cpsList.add(cpsV)

        println("<===================>")
        println("Спрос: " + demand)
        println("Итерация: $i   ЧПС: " + cpsV)

    }

    val M = cpsList.sum().div(cpsList.count())
//  Начало расчета дисперсия экспериментальное
    var Dx = 0.0
    for (cpsL in cpsList) {
        Dx += (cpsL - M).pow(2.0)
    }
    Dx = Dx.div(cpsList.count() - 1)
// Конец

    val intervals = (3.3 * log10(cpsList.size.toDouble()) + 1).toInt()
    val min = cpsList.reduce { a: Double, b: Double -> a.coerceAtMost(b) }
    val max = cpsList.reduce { a: Double, b: Double -> a.coerceAtLeast(b) }
    var step = (max - min).div(cpsList.size)
    var skip = 0.0
    cpsList.sort()

    for (i in 1..intervals) {
        val cpsListInterval = cpsList.stream()
            .skip(skip.toLong())
            .filter { d: Double -> d <= step }
            .toList()

        val countInInterval = cpsListInterval.count()

        if (countInInterval != 0) {
            val averageInInterval = cpsListInterval.sum() / countInInterval

            println("Интервал: $i; Число попаданий в интервал: $countInInterval; Среднее: ${averageInInterval.toInt()} тысч $")
        }
        step += step
        skip += countInInterval
    }

}

// Вычесление данных
fun dataV(demandL: MutableList<Int>): MutableList<Double> {
    var clearProfitL = mutableListOf<Double>()
    for (k in 0..years - 1) {
        supply = demandL[k] * sellPrice // Доход
        variableCosts = variableCostsProcent * supply //Переменные затраты
        profitBeforeTax = supply.minus(fixedCosts.plus(variableCosts.plus(depreciation))) // Прибиль до налога
        tax = profitBeforeTax * taxProcent // Налог
        profitAfterTax = profitBeforeTax.minus(tax) // Прибиль после налога
        clearProfitL.add(profitAfterTax + depreciation) // Чистая прибыль
    }
    return clearProfitL
}

fun cps(years: Int, coastOfCapital: Double, clearProfit: MutableList<Double>): Double {
    var count = 0.0
    for (i in 1..years) {
        count += clearProfit[i - 1].div((1 + coastOfCapital).pow(i))
    }
    return count
}

fun randomDemand(avgValue : Int, criticalValue: Int ): Int {
    val rand = Random()
    val minRandValue = avgValue - criticalValue
    val maxRandValue = avgValue + criticalValue
    val multiply = maxRandValue - minRandValue + 1
    return (minRandValue + rand.nextDouble() * multiply).toInt()
}

fun cummulitiveDistributionFunction(k: Int): Int {
    val lambda = 10.0
    val e = E.pow(-lambda)
    var sum = 0.0
    for (i in 0 .. k) {
        val n = lambda.pow(i).div(factorial(i))
        sum += n
    }
    return (e * sum).toInt()
}

private fun factorial(f : Int) : Double {
    var factorial = BigInteger.ONE
    for (i in 1..f) {
        factorial = factorial.multiply(BigInteger.valueOf(i.toLong()))
    }
    return factorial.toDouble();
}

