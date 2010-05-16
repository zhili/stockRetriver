// package main.scala
import scala.io.Source
import java.net.URL
import scala.actors._
import Actor._
object StockPriceFinder {
    def getLastestClosingPrice(symbol: String) = {
        val url = "http://ichart.finance.yahoo.com/table.csv?s=" +
            symbol + "&a=00&b=01&c=2010" 

        val data = Source.fromURL(url).mkString
        val mostRecentData = data.split("\n")(1)
        val closingPrice = mostRecentData.split(",")(4).toDouble
        //println(closingPrice)
        closingPrice
    }
    
    def getTickersAndUnits() = {
        val stocksAndUnitsXML = scala.xml.XML.load("stocks.xml")

        (Map[String, int]() /: (stocksAndUnitsXML \ "symbol")) { (map, symbolNode) =>
            val ticker = (symbolNode \ "@ticker").toString
            val units = (symbolNode \ "units").text.toInt 
            map(ticker) = units
        }
    }

    def main(args: Array[String]) {
        val symbolsAndUnits = getTickersAndUnits
        println("Today is " + new java.util.Date())
        println("Ticker Units Closing Price($) Total Value")
        val caller = self
        val startTime = System.nanoTime()
        val netWorth = (0.0 /: symbolsAndUnits) { (worth, symbolAndUnits) =>
            val (symbol, units) = symbolAndUnits
            val lastestClosingPrice = getLastestClosingPrice(symbol)
            val value = units * lastestClosingPrice
            println("%-7s %-5d %-16f %f".format(symbol, units, lastestClosingPrice, value))
            worth + value
        }
//      concurrent version.
//symbolsAndUnits.keys.foreach { symbol =>
//    actor { caller ! (symbol, getLastestClosingPrice(symbol)) }
//}
//
//val netWorth = (0.0 /: (1 to symbolsAndUnits.size) ) { (worth, index) =>
//    receiveWithin(10000) {
//        case (symbol: String, lastestClosingPrice: Double) =>
//        val units = symbolsAndUnits(symbol)
//        val value = units * lastestClosingPrice
//        println("%-7s %-5d %-16f %f".format(symbol, units, lastestClosingPrice, value))
//        worth + value
//    }
//}
        val endTime = System.nanoTime()
        println("Took of %f seconds".format((endTime-startTime)/1000000000.0))
    }
}

// vim: set ts=4 sw=4 et:
