object ws {
  val s1 = Some("hello")                          //> s1  : Some[String] = Some(hello)
  val s2 = Some("world")                          //> s2  : Some[String] = Some(world)
  val s3 = None                                   //> s3  : None.type = None
  val x = Set(s1, s2, s3)                         //> x  : scala.collection.immutable.Set[Option[String]] = Set(Some(hello), Some(
                                                  //| world), None)
  val xx = x.flatMap(x=>x)                        //> xx  : scala.collection.immutable.Set[String] = Set(hello, world)
  val xxx = x.flatten                             //> xxx  : scala.collection.immutable.Set[String] = Set(hello, world)
  val sss = List(s1,s2).flatten                   //> sss  : List[String] = List(hello, world)
  import play.api.libs.json._
  val json = Json.obj("type"->"chat", "player"->"Andrzej", "msg"->"Hello world!","x"->1);
                                                  //> json  : play.api.libs.json.JsObject = {"type":"chat","player":"Andrzej","msg
                                                  //| ":"Hello world!","x":1}
  val j1 = (json \ "type").asOpt[String]          //> j1  : Option[String] = Some(chat)
  val j2 = (json \ "x").asOpt[String]             //> j2  : Option[String] = None
  val j3 = (json \ "x").asOpt[Int]                //> j3  : Option[Int] = Some(1)
  val ar = Json.arr(1,2)                          //> ar  : play.api.libs.json.JsArray = [1,2]
  val w = ar match {
    case JsArray(Seq(JsNumber(a),JsNumber(b))) => a+b
    case _ => "!"
  }                                               //> w  : java.io.Serializable = 3
  
  def hello(value: Int) = value+1                 //> hello: (value: Int)Int
  
  val zzz = List(1,2,3).map(hello _)              //> zzz  : List[Int] = List(2, 3, 4)
  
  def bum(a: Int, b: Int, f: (Int,Int)=>Int) = f(a,b)
                                                  //> bum: (a: Int, b: Int, f: (Int, Int) => Int)Int
  def someF(a: Int, b: Int) = a+b                 //> someF: (a: Int, b: Int)Int
  val bumValue = bum(1,2,someF)                   //> bumValue  : Int = 3
  
  def kkk() = 1                                   //> kkk: ()Int
  def yyy(a: Any) = a match {
    case f: (()=>Int) => "funkcja"+f()
    case i: Int => "liczba"+i
    case _ => "coś"
  }                                               //> yyy: (a: Any)String
  val yyyValue = yyy(kkk _)                       //> yyyValue  : String = funkcja1
}