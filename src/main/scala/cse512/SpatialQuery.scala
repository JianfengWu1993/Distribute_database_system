package cse512

import org.apache.spark.sql.SparkSession

object SpatialQuery extends App{

  def ST_Contains(queryRectangle:String, pointString:String):Boolean={
    try {
      var rectangle = new Array[String](4)
      rectangle = queryRectangle.split(",")
      var rect_x1 = rectangle(0).trim.toDouble
      var rect_y1 = rectangle(1).trim.toDouble
      var rect_x2 = rectangle(2).trim.toDouble
      var rect_y2 = rectangle(3).trim.toDouble

      var point = new Array[String](2)
      point = pointString.split(",")
      var point_x = point(0).trim.toDouble
      var point_y = point(1).trim.toDouble


      var lower_x = math.min(rect_x1,rect_x2)
      var higher_x = math.max(rect_x1,rect_x2)

      var lower_y = math.min(rect_y1, rect_y2)
      var higher_y = math.max(rect_y1, rect_y2)

      if(point_y > higher_y || point_x < lower_x || point_x>higher_x || point_y < lower_y)
        return false
      else
        return true
    }
    catch {
      case _: Throwable => return false
    }
  }

  def ST_Within(pointString1:String, pointString2:String, distance:Double):Boolean={
    try {
      var pt1 = new Array[String](2)
      pt1 = pointString1.split(",")

      var pt1_x = pt1(0).trim.toDouble
      var pt1_y = pt1(1).trim.toDouble

      var pt2 = new Array[String](2)
      pt2 = pointString2.split(",")

      var pt2_x = pt2(0).trim.toDouble
      var pt2_y = pt2(1).trim.toDouble


      var pointDistance = Math.sqrt((pt1_x - pt2_x)*(pt1_x - pt2_x) + (pt1_y - pt2_y)*(pt1_y - pt2_y))

      if(pointDistance <= distance)
        return true
      else
        return false
    }
    catch {
      case _: Throwable => return false
    }
  }
  def runRangeQuery(spark: SparkSession, arg1: String, arg2: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
    pointDf.createOrReplaceTempView("point")

    // YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
    spark.udf.register("ST_Contains",(queryRectangle:String, pointString:String)=>((ST_Contains(queryRectangle, pointString))))

    val resultDf = spark.sql("select * from point where ST_Contains('"+arg2+"',point._c0)")
    resultDf.show()

    return resultDf.count()
  }

  def runRangeJoinQuery(spark: SparkSession, arg1: String, arg2: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
    pointDf.createOrReplaceTempView("point")

    val rectangleDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg2);
    rectangleDf.createOrReplaceTempView("rectangle")

    // YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
    spark.udf.register("ST_Contains",(queryRectangle:String, pointString:String)=>((ST_Contains(queryRectangle, pointString))))

    val resultDf = spark.sql("select * from rectangle,point where ST_Contains(rectangle._c0,point._c0)")
    resultDf.show()

    return resultDf.count()
  }

  def runDistanceQuery(spark: SparkSession, arg1: String, arg2: String, arg3: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
    pointDf.createOrReplaceTempView("point")

    // YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
    spark.udf.register("ST_Within",(pointString1:String, pointString2:String, distance:Double)=>((ST_Within(pointString1,pointString2,distance))))

    val resultDf = spark.sql("select * from point where ST_Within(point._c0,'"+arg2+"',"+arg3+")")
    resultDf.show()

    return resultDf.count()
  }

  def runDistanceJoinQuery(spark: SparkSession, arg1: String, arg2: String, arg3: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
    pointDf.createOrReplaceTempView("point1")

    val pointDf2 = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg2);
    pointDf2.createOrReplaceTempView("point2")

    // YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
    spark.udf.register("ST_Within",(pointString1:String, pointString2:String, distance:Double)=>((ST_Within(pointString1,pointString2,distance))))
    val resultDf = spark.sql("select * from point1 p1, point2 p2 where ST_Within(p1._c0, p2._c0, "+arg3+")")
    resultDf.show()

    return resultDf.count()
  }
}
