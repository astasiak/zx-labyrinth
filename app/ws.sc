object ws {
  val s1 = Some("hello")                          //> s1  : Some[String] = Some(hello)
  val s2 = Some("world")                          //> s2  : Some[String] = Some(world)
  val s3 = None                                   //> s3  : None.type = None
  val x = Set(s1, s2, s3)                         //> x  : scala.collection.immutable.Set[Option[String]] = Set(Some(hello), Some(
                                                  //| world), None)
  val xx = x.flatMap(x=>x)                        //> xx  : scala.collection.immutable.Set[String] = Set(hello, world)
}