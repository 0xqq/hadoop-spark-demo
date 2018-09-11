package spark

import java.net.{ConnectException, URL, URLDecoder}
import java.sql.{DriverManager, Timestamp}
import java.text.SimpleDateFormat
import java.util.{Date, Locale}
import org.apache.log4j.{Level, Logger}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.immutable.ListMap
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.xml.XML


/**
  * 实时统计用户标签数据
  * Created by zhaopf on 2018/08/18
  */
object userTag_phone {

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.WARN)
    @transient lazy val logger = Logger.getLogger(this.getClass)

    if (args.length < 3){
      System.err.print("the sparkstreaming job params is error，the parms is less 3")
      System.exit(1)
    }
    val Array(sparkname,brokers,topics) = args
    val conf=new SparkConf().setAppName(sparkname)
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.scheduler.mode", "FAIR") //设置调度策略为公平调度FAIR 默认是先进先出调度FIFO
      .set("spark.streaming.kafka.maxRatePerPartition", "10000") //往sparkConf中设置参数,maxRatePerPartition是指kafka消费数据的速
      .set("spark.shuffle.sort.bypassMergeThreshold","600")
      .set("spark.shuffle.file.buffer", "64")
//          .setMaster("local[4]")

    val sc = new SparkContext(conf)
    val ssc=new StreamingContext(sc,Seconds(1))

    // 进行过滤的集合：
    var tagListBuffer:ListBuffer[AppTag]=ListBuffer[AppTag]()  //标签集合
    var cityListBuffer:ListBuffer[City]=ListBuffer[City]()  //城市集合
    //定义模型model
    var model:NaiveBayesModel=null
    val listBuffer = ListBuffer[Tag]()   //批处理tag

    //进行逻辑判断的初始变量
    var hournumold5:Int=0
    var daynumold5:Int=0

    // 初始化mysql集合
    if(tagListBuffer.isEmpty&&cityListBuffer.isEmpty&&model==null){
      println("start begin init listBuffer!")
      tagListBuffer=query_appTag()//标签
      cityListBuffer=query_city() //城市
      model=training_Bayes(sc) //贝叶斯模型训练
    }

    // Kafka configurations     Kafka DirectAPI方式
    val topicsSet = topics.split(",").toSet

    var group="phone-group" //消费组id android

    /** kafka config */
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> brokers,
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> group,
      //      "auto.offset.reset" -> "earliest ",
      "auto.offset.reset" -> "latest",
      //      "auto.offset.reset" -> "none",
      "serializer.class" -> "kafka.serializer.StringEncoder",
      "enable.auto.commit" -> (true: java.lang.Boolean)
    )

    val lines = KafkaUtils.createDirectStream[String, String]( ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topicsSet, kafkaParams)).map(_.value())


    val resultRDDS=lines.map(x=>{
      //拆分日志 取不同字段
//      println("x=="+x)
      var appList="-999" //appList
      var userName="-999"
      var longitude="-999" //经度
      var latitude="-999"  //维度
      var city="-999"
      var poiName="-999"

      //格式化字符串 14/Aug/2018:15:24:57 +0800  ==》2018-08-14 15:24:57
      var logDate=x.split("\\]")(2).split("\\[")(1)
      val formatter = new SimpleDateFormat( "dd/MMM/yyyy:hh:mm:ss Z", Locale.ENGLISH )
      val date=formatter.parse( logDate )
      val ff = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss")
      logDate=ff.format(date) //日志时间

      //applist和address 都存在情况下
      if(x.contains("appList=")&&x.contains("&address=latitude")){
        //          println("数据="+(x))
        val appSp=x.split("appList=")(1).split("&address=latitude")(0).split("&")(0).split(",")
        appSp.foreach(a=>{
          val str=a.split(":")(0)
          if("-999".equals(appList)) appList=str
          else appList=appList+","+str
        })
        //切割字符串-》.split(",").mkString-》array转字符串
        //          println("appSp集合="+appSp.mkString)
        //          println("app集合appList="+appList)

        val addSp=x.split("&address=")(1).split("&")(0).split("]")(0)
        if(!addSp.contains("\\x")) {
         try{
           //#url encode解码
           val url = URLDecoder.decode( addSp, "UTF-8" )
           longitude = url.split( "longitude=" )( 1 ).split( "#" )( 0 )
           latitude = url.split( "latitude=" )( 1 ).split( "#" )( 0 )
           city = url.split( "city=" )( 1 ).split( "#" )( 0 )
           poiName = url.split( "poiName=" )( 1 ).split( "#" )( 0 )

           if (appList.isEmpty()) appList = "-999"
           if (city.isEmpty()) city = "-999"
           if (poiName.isEmpty()) poiName = "-999"
         } catch {
           case e: Exception => {
             logger.info("异常信息：" + e)
             logger.info("addSp：" + addSp)
           }

         }

        }

        if(appList.isEmpty()){
          //            println("url=="+url)
          //          println("x=="+x)
          //          println(" longitude="+longitude+" latitude="+latitude+" city="+city+" poiName="+poiName+" appList="+appList)
        }
      }else if(x.contains("appList=")){
        //          println("数据="+(x))
        var appSp=x.split("appList=")(1).split("&")(0).split(",")
        appSp.foreach(a=>{
          val str=a.split(":")(0)
          if("-999".equals(appList)) appList=str
          else appList=appList+","+str
        })
        if(appList.isEmpty()) appList="-999"
        //切割字符串-》.split(",").mkString-》array转字符串
        //          println("appSp集合="+appSp.mkString)
        //                    println("app集合appList="+appList)
      }
      if(x.contains("username=")) {
        val ss=x.split("username=")(1).split("&")
        if(ss.size>0) userName=ss(0)
        if(userName.contains("\\x")){
//          logger.info("userName="+userName)
          userName="-999"
        }
      }

      //                println("userName="+userName+" longitude="+longitude+" latitude="+latitude+" city="+city+" poiName="+poiName+" appList="+appList+" logDate="+logDate)
      var str="longitude="+longitude+"!!latitude="+latitude+""+"!!city="+city+"!!poiName="+poiName+"!!appList="+appList+"!!logDate="+logDate
      (userName,str)
    })

    //    println("resultRDDS="+resultRDDS.count())

    //-------------------------进行数据过滤-------userName-----applist----
    var frdd=resultRDDS.filter(x=>{
      var s1=x._1
      var s2=x._2
      var applist = ""
      val sp2=x._2.split("\\!\\!")(4).split("=")
      if(sp2.length>1)  applist=sp2(1)
      var flag: Boolean=true
      //对传入的每个值，在方法中判断flag是不是想要的，如果不是返回false，如果是返回true
      if("-999".equals(s1))flag=false;
      if(flag&&"-999".equals(applist)) flag=false;
      flag
    })
    //    println("frdd="+frdd.count())

    frdd.foreachRDD(rdd=>{
      //      println("rdd="+rdd.count().toInt)
      //-------------------------进行数据组装-------------------------
      val poirdd=rdd.mapPartitions(partiton=>{

        partiton.map(x=>{
          var cityCode="-999" //
          var poiType="-999"
          var s2=x._2
          //      println("原来的s2="+s2)
          val longitude=x._2.split("\\!\\!")(0).split("=")(1) //经度
          val latitude=x._2.split("\\!\\!")(1).split("=")(1)  //维度
          val city=x._2.split("\\!\\!")(2).split("=")(1)  //city
          val poiName=x._2.split("\\!\\!")(3).split("=")(1)
          var appList=x._2.split("\\!\\!")(4).split("=")(1)  //appList
          var logDate=x._2.split("\\!\\!")(5).split("=")(1)  //logDate

          var gdurl="-999"
          var flag: Boolean = true
          if(!"-999".equals(poiName)){ //poiName不为空的情况下 才调接口
            // ----------------调用第三方接口并用xml进行解析：高德api #拼接高德url接口--------------
            gdurl="https://restapi.amap.com/v3/geocode/regeo?output=xml&location="+longitude+","+latitude+"&key=86a85559a343ceb1f1ff25e6518bc7b0&radius=200&extensions=all"

            try {
              val url = new URL(gdurl)
              val conn = url.openConnection
              conn.setConnectTimeout(3000)
              conn.setReadTimeout(3000)
              val intputStream=conn.getInputStream
              if(intputStream!=None){
                var someXml = XML.load( conn.getInputStream )
                var cityCode = (someXml \ "regeocode" \ "addressComponent" \ "citycode").text
                if (cityCode.isEmpty) cityCode = "-999" //
                val pp = (someXml \\ "poi")
                //      println(someXml)
                //      println(citycode)
                //      println(pp)
                //筛选数据
                for (elem <- pp if flag) { //匹配上则，退出循环
                  val p2 = (elem \\ "name").text
                  flag = !poiName.equals( p2 )
                  if (!flag) poiType = (elem \\ "type").text
                  //        println( flag )
                  //        println( poiName.equals(p2))
                  //        println("poiType="+poiType)
                }
              }

            }catch {
              case e: ConnectException =>   //出现连接异常则再调一次接口
                val url = new URL(gdurl)
                val conn = url.openConnection
                conn.setConnectTimeout(3000)
                conn.setReadTimeout(3000)
                val intputStream=conn.getInputStream
                if(intputStream!=None){
                  var someXml = XML.load( conn.getInputStream )
                  var cityCode = (someXml \ "regeocode" \ "addressComponent" \ "citycode").text
                  if (cityCode.isEmpty) cityCode = "-999" //
                  val pp = (someXml \\ "poi")
                  //      println(someXml)
                  //      println(citycode)
                  //      println(pp)
                  //筛选数据
                  for (elem <- pp if flag) { //匹配上则，退出循环
                    val p2 = (elem \\ "name").text
                    flag = !poiName.equals( p2 )
                    if (!flag) poiType = (elem \\ "type").text
                    //        println( flag )
                    //        println( poiName.equals(p2))
                    //        println("poiType="+poiType)
                  }
                }
              case ex: Exception =>println(ex)
            }

          }

          //组装s2数据
          s2="cityCode="+cityCode+"!!city="+city+"!!poiType="+poiType+"!!poiName="+poiName+"!!appList="+appList+"!!logDate="+logDate
          /* if("-999".equals(poiType)&&(!"-999".equals(poiName))){
             println(gdurl)
             println("拼装好的s2="+s2)
           }*/
          //               println(gdurl)
          //              println("拼装好的s2="+s2)
          (x._1,s2)
        })

      })
      println("数据组装好的poirdd="+poirdd.count())

      //--------------------------------------进行打标签------------------------------------
      var finalRdd=poirdd.groupBy(item=>{
        item._1
      })
        .map(x=>{
          //定义对象累加器
          var age1:Double=0;
          var age2:Double=0;
          var age3:Double=0;
          var age4:Double=0;
          var age5:Double=0;
          var age6:Double=0;
          var age7:Double=0;
          var age8:Double=0;
          var age9:Double=0;
          var male:Double=0;
          var female:Double=0;
          var free:Double=0;
          var pirate:Double=0;
          var comic:Double=0;
          var live:Double=0;
          var photo:Double=0;
          var game:Double=0;
          var game_ancient:Double=0;
          var game_xianxia:Double=0;
          var game_reason:Double=0;
          var game_panic:Double=0;
          var game_ri:Double=0;
          var hand_play:Double=0;
          var servant:Double=0;
          var internet_job:Double=0;
          var techer:Double=0;
          var student:Double=0;
          var mother_job:Double=0;
          var female_job:Double=0;
          var movie:Double=0;
          var money:Double=0;
          var child:Double=0;
          var car:Double=0;
          var war:Double=0;
          var social:Double=0;
          var home:Double=0;
          var study:Double=0;
          var publish:Double=0;
          var feudal:Double=0;
          var tomb:Double=0;
          var information:Double=0;
          var antique:Double=0;
          var music:Double=0;
          var watching:Double=0;
          var star:Double=0;
          var love:Double=0;
          var homemaking:Double=0;
          var shopping:Double=0;
          var shaping:Double=0;
          var wasteful:Double=0;
          var makeup:Double=0;
          var oversea_shop:Double=0;
          var tourism:Double=0;
          var listen_book:Double=0;
          var quadratic:Double=0;
          var low_age:Double=0;
          var pet:Double=0;
          var out_food:Double=0;
          var video:Double=0;

          //定义城市等级
          var cityLev=0; //默认定义城市等级为0
          var cityCode="-999"; //
          var city="-999"; //
          var diyu="-999"; //定义地域类型标签
          var logDate=""; //logDate

          //      println("分组数据："+x)
          val s2list=x._2.toList
          s2list.foreach(row=>{
            cityCode=row._2.split("\\!\\!")(0).split("=")(1) //
            city=row._2.split("\\!\\!")(1).split("=")(1) //
            val poiType=row._2.split("\\!\\!")(2).split("=")(1) //
            //        val poiName=row._2.split("\\!\\!")(2).split("=")(1) //
            val appList=row._2.split("\\!\\!")(4).split("=")(1).split(",") //appList
            logDate=row._2.split("\\!\\!")(5).split("=")(1) //logDate

            //进行地域标签评定
            if(!"-999".equals(poiType)){
              if(poiType.contains("学校")) diyu="学校"
              else if(poiType.contains("住宅区")) diyu="小区"
              else if(poiType.contains("网吧")) diyu="网吧"
            }

            //进行城市等级评定
            if(!"-999".equals(cityCode)&&(!"-999".equals(city))){
              var flag: Boolean=true
              for (elem <- cityListBuffer if flag) {
                if(cityCode.equals(elem.city_code)){
                  flag=false
                  cityLev=elem.city_level //mysql城市等级12345级
                }

                if(city.equals(elem.city_name)){
                  flag=false
                  cityLev=elem.city_level //城市等级12345级
                }

              }
            }
            //   ---------applist 进行累加和
            appList.foreach(line=>{
              tagListBuffer.foreach(app=>{
                val app_id = app.app_id
                if(line.equals(app_id)){ //如果app相同，则进行各项累加
                  age1+=app.age1
                  age2+=app.age2
                  age3+=app.age3
                  age4+=app.age4
                  age5+=app.age5
                  age6+=app.age6
                  age7+=app.age7
                  age8+=app.age8
                  age9+=app.age9
                  male+=app.male
                  female+=app.female
                  free+=app.free
                  pirate+=app.pirate
                  comic+=app.comic
                  live+=app.live
                  photo+=app.photo
                  game+=app.game
                  game_ancient+=app.game_ancient
                  game_xianxia+=app.game_xianxia
                  game_reason+=app.game_reason
                  game_panic+=app.game_panic
                  game_ri+=app.game_ri
                  hand_play+=app.hand_play
                  servant+=app.servant
                  internet_job+=app.internet_job
                  techer+=app.techer
                  student+=app.student
                  mother_job+=app.mother_job
                  female_job+=app.female_job
                  movie+=app.movie
                  money+=app.money
                  child+=app.child
                  car+=app.car
                  war+=app.war
                  social+=app.social
                  home+=app.home
                  study+=app.study
                  publish+=app.publish
                  feudal+=app.feudal
                  tomb+=app.tomb
                  information+=app.information
                  antique+=app.antique
                  music+=app.music
                  watching+=app.watching
                  star+=app.star
                  love+=app.love
                  homemaking+=app.homemaking
                  shopping+=app.shopping
                  shaping+=app.shaping
                  wasteful+=app.wasteful
                  makeup+=app.makeup
                  oversea_shop+=app.oversea_shop
                  tourism+=app.tourism
                  listen_book+=app.listen_book
                  quadratic+=app.quadratic
                  low_age+=app.low_age
                  pet+=app.pet
                  out_food+=app.out_food
                  video+=app.video
                }
              })
            }) //集合遍历结束
          }) //分组后遍历s2list end

          /**------------------进行判断逻辑 赋值向量--正式打标签-----start----*/
          //定义向量
          val ab=ArrayBuffer[Double]()
          if(age1>1) ab+=1.0 else ab+=0
          if(age2>1) ab+=1.0 else ab+=0
          if(age3>1) ab+=1.0 else ab+=0
          if(age4>1) ab+=1.0 else ab+=0
          if(age5>1) ab+=1.0 else ab+=0
          if(age6>1) ab+=1.0 else ab+=0
          if(age7>1) ab+=1.0 else ab+=0
          if(age8>1) ab+=1.0 else ab+=0
          if(age9>1) ab+=1.0 else ab+=0
          if(male>female) ab+=1.0 else ab+=0 //男的分值大于女的则赋值为该标签为男
          if(female>male) ab+=1.0 else ab+=0
          if(free>1) ab+=1.0 else ab+=0
          if(pirate>1) ab+=1.0 else ab+=0
          if(comic>1) ab+=1.0 else ab+=0
          if(live>1) ab+=1.0 else ab+=0
          if(photo>1) ab+=1.0 else ab+=0
          if(game>1) ab+=1.0 else ab+=0
          if(game_ancient>1) ab+=1.0 else ab+=0
          if(game_xianxia>1) ab+=1.0 else ab+=0
          if(game_reason>1) ab+=1.0 else ab+=0
          if(game_panic>1) ab+=1.0 else ab+=0
          if(game_ri>1) ab+=1.0 else ab+=0
          if(hand_play>1) ab+=1.0 else ab+=0
          if(servant>1) ab+=1.0 else ab+=0
          if(internet_job>1) ab+=1.0 else ab+=0
          if(techer>1) ab+=1.0 else ab+=0
          if(student>1) ab+=1.0 else ab+=0
          if(mother_job>1) ab+=1.0 else ab+=0
          if(female_job>1) ab+=1.0 else ab+=0
          if(movie>1) ab+=1.0 else ab+=0
          if(money>1) ab+=1.0 else ab+=0
          if(child>1) ab+=1.0 else ab+=0
          if(car>1) ab+=1.0 else ab+=0
          if(war>1) ab+=1.0 else ab+=0
          if(social>1) ab+=1.0 else ab+=0
          if(home>1) ab+=1.0 else ab+=0
          if(study>1) ab+=1.0 else ab+=0
          if(publish>1) ab+=1.0 else ab+=0
          if(feudal>1) ab+=1.0 else ab+=0
          if(tomb>1) ab+=1.0 else ab+=0
          if(information>1) ab+=1.0 else ab+=0
          if(antique>1) ab+=1.0 else ab+=0
          if(music>1) ab+=1.0 else ab+=0
          if(watching>1) ab+=1.0 else ab+=0
          if(star>1) ab+=1.0 else ab+=0
          if(love>1) ab+=1.0 else ab+=0
          if(homemaking>1) ab+=1.0 else ab+=0
          if(shopping>1) ab+=1.0 else ab+=0
          if(shaping>1) ab+=1.0 else ab+=0
          if(wasteful>1) ab+=1.0 else ab+=0
          if(makeup>1) ab+=1.0 else ab+=0
          if(oversea_shop>1) ab+=1.0 else ab+=0
          if(tourism>1) ab+=1.0 else ab+=0
          if(listen_book>1) ab+=1.0 else ab+=0
          if(quadratic>1) ab+=1.0 else ab+=0
          if(low_age>1) ab+=1.0 else ab+=0
          if(pet>1) ab+=1.0 else ab+=0
          if(out_food>1) ab+=1.0 else ab+=0
          if(video>1) ab+=1.0 else ab+=0

          //维护地域标签(3个标签)、城市等级标签（5个标签）
          if(diyu.equals("-999")){
            ab+=1.0
            ab+=1.0
            ab+=1.0  //维护3个标签
          }else{
            if(diyu.contains("学校")) ab+=1.0 else ab+=0
            if(diyu.contains("小区")) ab+=1.0 else ab+=0
            if(diyu.contains("网吧")) ab+=1.0 else ab+=0
          }
          if(cityLev==0){ //维护5个标签
            ab+=1.0
            ab+=1.0
            ab+=1.0
            ab+=1.0
            ab+=1.0
          } else{
            if(cityLev==1) ab+=1.0 else ab+=0
            if(cityLev==2) ab+=1.0 else ab+=0
            if(cityLev==3) ab+=1.0 else ab+=0
            if(cityLev==4) ab+=1.0 else ab+=0
            if(cityLev==5) ab+=1.0 else ab+=0
          }
          var catalog=0;
          if(!ab.isEmpty&&model!=null){
            val vc = Vectors.dense(ab.toArray) //转成向量
            //      println("vc size="+vc.size)
            //      println("向量="+vc)
             catalog=model.predict(vc).toInt //进行学习计算，拿到分类id
            //        println("catalog="+catalog)

          }

          //进行拼接标签
          /*****************按照规则进行排序 高-》低 筛选字段******************************/

          /*    '6-12','12-16','16-18','18-23','23-28','28-33','33-38','38-48','48+','male','female','免费 ','盗版','漫画','直播','摄影','游戏','游戏古风 ','游戏仙侠 ','游戏推理解密  ','游戏惊悚  ','游戏日系   ','手办','公务员','互联网从业者','教师','学生','全职妈妈','职业女性 ','电影','理财','有孩子','有车 ','军事','社交','宅','爱学习','出版物','封建迷信','盗墓小说','资讯','古玩','音乐','追剧','追星','恋爱','家政','购物','整形','轻奢','美妆','海淘','旅游','听书','二次元','低龄','宠物','外卖','视频','小区','学校 ','网吧','地域级别1 ','地域级别2','地域级别3','地域级别4','地域级别5'*/

          //定义标签类
          var tagMap:scala.collection.immutable.Map [String,String]=Map(
            "age1"->"6-12", "age2"->"12-16", "age3"->"16-18", "age4"->"18-23", "age5"->"23-28",
            "age6"->"28-33", "age7"->"33-38", "age8"->"38-48", "age9"->"48+","servant"->"公务员","internet_job"->"互联网从业者","techer"->"教师","student"->"学生","mother_job"->"全职妈妈","female_job"->"职业女性")

          //用户名
          val userName=x._1
          //          println("性别得分：male="+male+" female="+female)
          //性别
          var gender="-999"
          if(male>female) gender="0" //男
          if(female>male) gender="1" //女

          //年龄
          var ageMap:scala.collection.immutable.Map [String,Double]=Map(
            "age1"->age1, "age2"->age2, "age3"->age3, "age4"->age4, "age5"->age5,
            "age6"->age6, "age7"->age7, "age8"->age8, "age9"->age9
          )
          var sortMap_age:scala.collection.immutable.ListMap[String,Double]= ListMap(ageMap.toSeq.sortWith(_._2 > _._2):_*)
          var age=sortMap_age.keys.take(1).mkString
          age=tagMap(age)

          //职业
          var professionMap:scala.collection.immutable.Map [String,Double]=Map(
            "servant"->servant, "internet_job"->internet_job, "techer"->techer, "student"->student, "mother_job"->mother_job, "female_job"->female_job
          )
          var sortMap_profession:scala.collection.immutable.ListMap[String,Double]= ListMap(professionMap.toSeq.sortWith(_._2 > _._2):_*)
          var profession=sortMap_profession.keys.take(1).mkString
          profession=tagMap(profession)

          //标签str 拼接标签
          var tag=""
          if(free>1) tag="免费"
          if(pirate>1) if(tag.size>0) tag+=","+"盗版" else tag+="盗版"
          if(comic>1)  if(tag.size>0) tag+=","+"漫画"  else tag+="漫画"
          if(live>1)  if(tag.size>0) tag+=","+"直播" else tag+="直播"
          if(photo>1) if(tag.size>0) tag+=","+"摄影"  else tag+="摄影"
          if(game>1)  if(tag.size>0) tag+=","+"游戏" else tag+="游戏"
          if(game_ancient>1) if(tag.size>0)  tag+=","+"游戏古风" else tag+="游戏古风"
          if(game_xianxia>1) if(tag.size>0) tag+=","+"游戏仙侠" else tag+="游戏仙侠"
          if(game_reason>1)  if(tag.size>0) tag+=","+"游戏推理解密" else tag+="游戏推理解密"
          if(game_panic>1) if(tag.size>0) tag+=","+"游戏惊悚" else tag+="游戏惊悚"
          if(game_ri>1)  if(tag.size>0) tag+=","+"游戏日系" else tag+="游戏日系"
          if(hand_play>1)  if(tag.size>0) tag+=","+"手办" else tag+="手办"
          if(movie>1)  if(tag.size>0) tag+=","+"电影" else tag+="电影"
          if(money>1)  if(tag.size>0) tag+=","+"理财" else tag+="理财"
          if(child>1) if(tag.size>0) tag+=","+"有孩子" else tag+="有孩子"
          if(car>1)  if(tag.size>0) tag+=","+"有车" else tag+="有车"
          if(war>1)  if(tag.size>0) tag+=","+"军事" else tag+="军事"
          if(social>1) if(tag.size>0) tag+=","+"社交" else tag+="社交"
          if(home>1)  if(tag.size>0) tag+=","+"宅"  else tag+="宅"
          if(study>1)  if(tag.size>0) tag+=","+"爱学习"  else tag+="爱学习"
          if(publish>1)  if(tag.size>0) tag+=","+"出版物"  else tag+="出版物"
          if(feudal>1)  if(tag.size>0) tag+=","+"封建迷信"  else tag+="封建迷信"
          if(tomb>1)  if(tag.size>0) tag+=","+"盗墓小说"  else tag+="盗墓小说"
          if(information>1)  if(tag.size>0) tag+=","+"资讯"  else tag+="资讯"
          if(antique>1)  if(tag.size>0) tag+=","+"古玩"  else tag+="古玩"
          if(music>1)  if(tag.size>0) tag+=","+"音乐"  else tag+="音乐"
          if(watching>1) if(tag.size>0) tag+=","+"追剧"  else tag+="追剧"
          if(star>1) if(tag.size>0) tag+=","+"追星" else tag+="追星"
          if(love>1)  if(tag.size>0) tag+=","+"恋爱" else tag+="恋爱"
          if(homemaking>1)  if(tag.size>0) tag+=","+"家政" else tag+="家政"
          if(shopping>1)  if(tag.size>0) tag+=","+"购物" else tag+="购物"
          if(shaping>1)  if(tag.size>0) tag+=","+"整形" else tag+="整形"
          if(wasteful>1)  if(tag.size>0) tag+=","+"轻奢" else tag+="轻奢"
          if(makeup>1)  if(tag.size>0) tag+=","+"美妆" else tag+="美妆"
          if(oversea_shop>1)  if(tag.size>0) tag+=","+"海淘" else tag+="海淘"
          if(tourism>1)  if(tag.size>0) tag+=","+"旅游" else tag+="旅游"
          if(listen_book>1)  if(tag.size>0) tag+=","+"听书" else tag+="听书"
          if(quadratic>1)  if(tag.size>0) tag+=","+"二次元" else tag+="二次元"
          if(low_age>1)  if(tag.size>0) tag+=","+"低龄" else tag+="低龄"
          if(pet>1)  if(tag.size>0) tag+=","+"宠物" else tag+="宠物"
          if(out_food>1) if(tag.size>0) tag+=","+"外卖" else tag+="外卖"
          if(video>1) if(tag.size>0) tag+=","+"视频" else tag+="视频"

          if("".equals(tag)) tag="-999"

          var cityLevel=""
          if(cityLev==0) cityLevel="-999"
          else cityLevel=cityLev.toInt.toString
          var catalogId=""
          if(catalog==0) catalogId="-999"
          else catalogId=catalog.toInt.toString

          /**------------------进行判断逻辑 赋值向量--正式打标签---end------*/
          //组装backStr数据
          val backStr="userName="+userName+"!!gender="+gender+"!!age="+age+"!!profession="+profession+"!!tag="+tag+"!!catalogId="+catalogId+"!!cityCode="+cityCode+"!!cityName="+city+"!!cityLevel="+cityLevel+"!!diyu="+diyu+"!!logDate="+logDate
          //          println("backStr="+backStr)

          val tagInfo=new Tag
          tagInfo.userName=userName
          tagInfo.gender=gender
          tagInfo.age=age
          tagInfo.profession=profession
          tagInfo.tag=tag
          tagInfo.catalogId=catalogId
          tagInfo.cityCode=cityCode
          tagInfo.cityName=city
          tagInfo.cityLevel=cityLevel
          tagInfo.diyu=diyu
          tagInfo.logDate=logDate

          listBuffer.append(tagInfo)

          //          println("listBuffer1===="+listBuffer.size)
          if(listBuffer.size>=1){
            //批处理tag实体类
            mysql_tag(listBuffer)
            //清空数据
            listBuffer.clear()
          }


          var now: Date = new Date()
          var dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss")
          var day = dateFormat.format(now)
          var daynum = day.split(":")(2).toInt
          var hournum = day.split(":")(3).toInt


          if ((daynum == daynumold5) && (hournum == hournumold5)) {
            hournumold5 = hournum
            daynumold5 = daynum
          }else if ((daynum == daynumold5)) {
            hournumold5 = hournum
            daynumold5 = daynum
            //清空
            tagListBuffer.clear()//标签
            cityListBuffer.clear() //城市
            model=null //贝叶斯模型训练
          }
          else {
            if(hournum==0) {  //凌晨逻辑处理
              //清空
              tagListBuffer.clear()//标签
              cityListBuffer.clear() //城市
              model=null //贝叶斯模型训练
            }
            hournumold5 = hournum
            daynumold5 = daynum
          }

          (userName,backStr)
        })
      println("finalRdd="+finalRdd.count())
    })


    ssc.start()
    ssc.awaitTermination()
    sc.stop()
  }


  /**
    * 训练贝叶斯模型
    * @author zhaopf
    *
    */
  def training_Bayes(sc:SparkContext):NaiveBayesModel ={
    //    val data = sc.textFile("hdfs://10.89.11.159:8020/tools/book_type_bayes/native_bayes_data.txt")
    val data = sc.textFile("hdfs://bicluster/tools/book_type_bayes/native_bayes_data.txt")
    //    val data = sc.textFile("d:\\ifengWork\\sample_native_bayes_data.txt")

    //读取数据 划分为分类和特征向量
    val parseData =data.map { line =>
      val parts = line.split(',')
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split('\t').map(_.toDouble)))
    }

    // 样本数据划分为训练样本和测试样本
    val splits=parseData.randomSplit(Array(0.6,0.4),seed=11l)
    //val training =splits(0)
    val training = parseData  //全部进行训练
    //    val test = splits(1)

    //新建贝叶斯分类模型 并训练
    val model = NaiveBayes.train(training,lambda = 1.0,modelType = "multinomial")
    return model
  }


  /**
    * 批处理标签表

    */
  def mysql_tag(listBuffer:ListBuffer[Tag]):Unit ={
    //生产账号
    val jdbc = "jdbc:mysql://10.89.13.158:3306/bi_report?user=readtongji&password=uZpvSuDuEiEOwCtk21on"
    val conn = DriverManager.getConnection(jdbc)
    val driver = "com.mysql.jdbc.Driver"
    val url = "jdbc:mysql://10.89.13.158:3306/bi_report"
    val username = "readtongji"
    val password = "uZpvSuDuEiEOwCtk21on"

    //测试账号
    //    val jdbc = "jdbc:mysql://10.89.13.158:3306/test?user=bi_test&password=uZpvSuDuEiEOwCtk21on123"
    //    val conn = DriverManager.getConnection(jdbc)
    //    val driver = "com.mysql.jdbc.Driver"
    //    val url = "jdbc:mysql://10.89.13.158:3306/test"
    //    val username = "bi_test"
    //    val password = "uZpvSuDuEiEOwCtk21on123"

    try {
      Class.forName(driver)
      val connection = DriverManager.getConnection(url, username, password)
      val prep = conn.prepareStatement("insert into recommend_user_tag (user_name,gender,age,profession,tags,catalog_id,city_code,city_name,city_level,diyu,log_date) values (?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE user_name=values(user_name), gender=values(gender), age=values(age), profession=values(profession), tags=values(tags), catalog_id=values(catalog_id), city_code=values(city_code), city_name=values(city_name), city_level=values(city_level), diyu=values(diyu), log_date=values(log_date);")

      listBuffer.foreach(x=>{
        val userName=x.userName
        val gender=x.gender
        val age=x.age
        val profession=x.profession
        val tag=x.tag
        val catalogId=x.catalogId
        val cityCode=x.cityCode
        val cityName=x.cityName
        val cityLevel=x.cityLevel
        val diyu=x.diyu
        val logDate=x.logDate
        println("userName111="+userName)
        prep.setString(1, userName)
        prep.setString(2, gender)
        prep.setString(3, age)
        prep.setString(4, profession)
        prep.setString(5, tag)
        prep.setString(6, catalogId)
        prep.setString(7, cityCode)
        prep.setString(8, cityName)
        prep.setString(9, cityLevel)
        prep.setString(10, diyu)
        prep.setTimestamp(11,Timestamp.valueOf(logDate) )

        //        prep.executeUpdate()
        prep.addBatch()
      })
      prep.executeBatch()
      //
    } catch {
      case e:Throwable => {
        println("FunnelGO failed! Coursed by getFunnels error")
        e.printStackTrace()
      }
    } finally {
      conn.close
    }
  }

  /**
    * 查询app标签表
    * @author zhaopf
    *
    */
  def query_appTag():ListBuffer[AppTag] ={
    //生产账号
    val jdbc = "jdbc:mysql://10.89.13.158:3306/bi_report?user=readtongji&password=uZpvSuDuEiEOwCtk21on"
    val conn = DriverManager.getConnection(jdbc)
    val driver = "com.mysql.jdbc.Driver"
    val url = "jdbc:mysql://10.89.13.158:3306/bi_report"
    val username = "readtongji"
    val password = "uZpvSuDuEiEOwCtk21on"

    //测试账号
    //    val jdbc = "jdbc:mysql://10.89.13.158:3306/test?user=bi_test&password=uZpvSuDuEiEOwCtk21on123"
    //    val conn = DriverManager.getConnection(jdbc)
    //    val driver = "com.mysql.jdbc.Driver"
    //    val url = "jdbc:mysql://10.89.13.158:3306/test"
    //    val username = "bi_test"
    //    val password = "uZpvSuDuEiEOwCtk21on123"

    val listBuffer = new ListBuffer[AppTag]

    //计算查询时间
    try {
      Class.forName(driver)
      val connection = DriverManager.getConnection(url, username, password)
      val prep = connection.prepareStatement("SELECT * from app_tag;")
      val rSet=prep.executeQuery()

      while(rSet.next()){
        val app_id=rSet.getString("app_id")
        val app_name=rSet.getString("app_name")
        val age1=rSet.getDouble("age1")
        val age2=rSet.getDouble("age2")
        val age3=rSet.getDouble("age3")
        val age4=rSet.getDouble("age4")
        val age5=rSet.getDouble("age5")
        val age6=rSet.getDouble("age6")
        val age7=rSet.getDouble("age7")
        val age8=rSet.getDouble("age8")
        val age9=rSet.getDouble("age9")
        val male=rSet.getDouble("male")
        val female=rSet.getDouble("female")
        val free=rSet.getDouble("free")
        val pirate=rSet.getDouble("pirate")
        val comic=rSet.getDouble("comic")
        val live=rSet.getDouble("live")
        val photo=rSet.getDouble("photo")
        val game=rSet.getDouble("game")
        val game_ancient=rSet.getDouble("game_ancient")
        val game_xianxia=rSet.getDouble("game_xianxia")
        val game_reason=rSet.getDouble("game_reason")
        val game_panic=rSet.getDouble("game_panic")
        val game_ri=rSet.getDouble("game_ri")
        val hand_play=rSet.getDouble("hand_play")
        val servant=rSet.getDouble("servant")
        val internet_job=rSet.getDouble("internet_job")
        val techer=rSet.getDouble("techer")
        val student=rSet.getDouble("student")
        val mother_job=rSet.getDouble("mother_job")
        val female_job=rSet.getDouble("female_job")
        val movie=rSet.getDouble("movie")
        val money=rSet.getDouble("money")
        val child=rSet.getDouble("child")
        val car=rSet.getDouble("car")
        val war=rSet.getDouble("war")
        val social=rSet.getDouble("social")
        val home=rSet.getDouble("home")
        val study=rSet.getDouble("study")
        val publish=rSet.getDouble("publish")
        val feudal=rSet.getDouble("feudal")
        val tomb=rSet.getDouble("tomb")
        val information=rSet.getDouble("information")
        val antique=rSet.getDouble("antique")
        val music=rSet.getDouble("music")
        val watching=rSet.getDouble("watching")
        val star=rSet.getDouble("star")
        val love=rSet.getDouble("love")
        val homemaking=rSet.getDouble("homemaking")
        val shopping=rSet.getDouble("shopping")
        val shaping=rSet.getDouble("shaping")
        val wasteful=rSet.getDouble("wasteful")
        val makeup=rSet.getDouble("makeup")
        val oversea_shop=rSet.getDouble("oversea_shop")
        val tourism=rSet.getDouble("tourism")
        val listen_book=rSet.getDouble("listen_book")
        val quadratic=rSet.getDouble("quadratic")
        val low_age=rSet.getDouble("low_age")
        val pet=rSet.getDouble("pet")
        val out_food=rSet.getDouble("out_food")
        val video=rSet.getDouble("video")

        var app=new AppTag()
        app.app_id=app_id
        app.app_name=app_name
        app.age1=age1
        app.age2=age2
        app.age3=age3
        app.age4=age4
        app.age5=age5
        app.age6=age6
        app.age7=age7
        app.age8=age8
        app.age9=age9
        app.male=male
        app.female=female
        app.free=free
        app.pirate=pirate
        app.comic=comic
        app.live=live
        app.photo=photo
        app.game=game
        app.game_ancient=game_ancient
        app.game_xianxia=game_xianxia
        app.game_reason=game_reason
        app.game_panic=game_panic
        app.game_ri=game_ri
        app.hand_play=hand_play
        app.servant=servant
        app.internet_job=internet_job
        app.techer=techer
        app.student=student
        app.mother_job=mother_job
        app.female_job=female_job
        app.movie=movie
        app.money=money
        app.child=child
        app.car=car
        app.war=war
        app.social=social
        app.home=home
        app.study=study
        app.publish=publish
        app.feudal=feudal
        app.tomb=tomb
        app.information=information
        app.antique=antique
        app.music=music
        app.watching=watching
        app.star=star
        app.love=love
        app.homemaking=homemaking
        app.shopping=shopping
        app.shaping=shaping
        app.wasteful=wasteful
        app.makeup=makeup
        app.oversea_shop=oversea_shop
        app.tourism=tourism
        app.listen_book=listen_book
        app.quadratic=quadratic
        app.low_age=low_age
        app.pet=pet
        app.out_food=out_food
        app.video=video
        listBuffer.append(app)
      }
    }
    finally{
      conn.close
    }
    return listBuffer
  }
  /**
    * 查询city等级表
    * @author zhaopf
    *
    */
  def query_city():ListBuffer[City] ={
    //生产账号
    val jdbc = "jdbc:mysql://10.89.13.158:3306/bi_report?user=readtongji&password=uZpvSuDuEiEOwCtk21on"
    val conn = DriverManager.getConnection(jdbc)
    val driver = "com.mysql.jdbc.Driver"
    val url = "jdbc:mysql://10.89.13.158:3306/bi_report"
    val username = "readtongji"
    val password = "uZpvSuDuEiEOwCtk21on"

    //测试账号
    //    val jdbc = "jdbc:mysql://10.89.13.158:3306/test?user=bi_test&password=uZpvSuDuEiEOwCtk21on123"
    //    val conn = DriverManager.getConnection(jdbc)
    //    val driver = "com.mysql.jdbc.Driver"
    //    val url = "jdbc:mysql://10.89.13.158:3306/test"
    //    val username = "bi_test"
    //    val password = "uZpvSuDuEiEOwCtk21on123"

    val listBuffer = new ListBuffer[City]

    //计算查询时间
    try {
      Class.forName(driver)
      val connection = DriverManager.getConnection(url, username, password)
      val prep = connection.prepareStatement("SELECT * from city_info;")
      val rSet=prep.executeQuery()

      while(rSet.next()){
        val id=rSet.getInt("id")
        val city_code=rSet.getInt("city_code")
        val city_name=rSet.getString("city_name")
        val city_level=rSet.getInt("city_level")

        var city=new City()
        city.id=id
        city.city_code=city_code
        city.city_name=city_name
        city.city_code=city_code
        city.city_level=city_level

        listBuffer.append(city)
      }
    } catch {
      case e:Throwable => {
        println("FunnelGO failed! Coursed by getFunnels error")
        e.printStackTrace()
      }
    } finally {
      conn.close
    }
    return listBuffer
  }

}

/**
  * app标签实体类
  */
class AppTag extends Serializable{
  var app_id:String = ""//app英文
  var app_name:String = "" //app中文
  var age1:Double = 0//6-12
  var age2:Double = 0//12-16
  var age3:Double = 0//16-18
  var age4:Double = 0//18-23
  var age5:Double = 0//23-28
  var age6:Double = 0//28-33
  var age7:Double = 0//33-38
  var age8:Double = 0//38-48
  var age9:Double = 0//48+
  var male:Double = 0//男
  var female:Double = 0//女
  var free:Double = 0 //免费
  var pirate:Double = 0 //盗版
  var comic:Double = 0 //漫画
  var live:Double = 0 //直播
  var photo:Double = 0 //摄影
  var game:Double = 0 //游戏
  var game_ancient:Double = 0 //游戏古风
  var game_xianxia:Double = 0 //游戏仙侠
  var game_reason:Double = 0 //游戏推理解密
  var game_panic:Double = 0 //游戏惊悚
  var game_ri:Double = 0 //游戏日系
  var hand_play:Double = 0 //手办
  var servant:Double = 0 //公务员
  var internet_job:Double = 0 //互联网工作
  var techer:Double = 0 //老师
  var student:Double = 0 //学生
  var mother_job:Double = 0 //职业妈妈
  var female_job:Double = 0 //职业女性
  var movie:Double = 0 //电影
  var money:Double = 0 //理财
  var child:Double = 0 //有孩子
  var car:Double = 0 //有车
  var war:Double = 0 //军事
  var social:Double = 0 //社交
  var home:Double = 0 //宅
  var study:Double = 0 //爱学习
  var publish:Double = 0 //出版物
  var feudal:Double = 0 //封建迷信
  var tomb:Double = 0 //盗墓
  var information:Double = 0 //资讯
  var antique:Double = 0 //古玩
  var music:Double = 0 //音乐
  var watching:Double = 0 //追剧
  var star:Double = 0 //追星
  var love:Double = 0 //恋爱
  var homemaking:Double = 0 //家政
  var shopping:Double = 0 //购物
  var shaping:Double = 0 //整形
  var wasteful:Double = 0 //轻奢
  var makeup:Double = 0 //美妆
  var oversea_shop:Double = 0 //海淘
  var tourism:Double = 0 //旅游
  var listen_book:Double = 0 //听书
  var quadratic:Double = 0 //二次元
  var low_age:Double = 0 //低龄
  var pet:Double = 0 //宠物
  var out_food:Double = 0 //外卖
  var video:Double = 0 //视频
}

/**
  * app标签实体类
  */
class City extends Serializable{
  var id:Int = 0//
  var city_code:Int = 0 //a
  var city_name:String = ""//
  var city_level:Int = 0//城市等级
}
/**
  * tag
  */
class Tag extends Serializable{
  var userName:String = ""//
  var gender:String = ""//性别 0:男 1：女
  var age:String = ""//年龄
  var profession:String = ""//职业
  var tag:String = ""//标签
  var catalogId:String = ""//喜好分类
  var cityCode:String = ""//城市编码
  var cityName:String = ""//城市名称
  var cityLevel:String = ""//城市等级
  var diyu:String = ""//地域：小区、学校、网吧
  var logDate:String = ""//日志时间
}
