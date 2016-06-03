package jpush

import java.io.{DataInputStream, File, FileOutputStream}
import java.net.ServerSocket

import scala.collection.JavaConversions._

/**
  * Created by dingb on 2016/6/3.
  */
object Server extends  App {
  def port = 8899
  override def main(args: Array[String]) {
    val roots = args.toList match {
      case Nil => List(new File(System.getProperty("user.dir")))
      case _ =>  args.map(new File(_)).toList
    }

    printf("dst root is %s\n", roots.mkString(","))
    val ss = new ServerSocket(port)
    def accept: Unit = {
      val s = ss.accept()
      printf("%s in \n", s.getRemoteSocketAddress);
      async {
        val dis = new DataInputStream(s.getInputStream)
        val fn = dis.readUTF
        val size = dis.readLong
        printf("loading %s %d\n", fn, size);
        if(size > 0) {
          roots.foreach(new File(_, fn).getParentFile.mkdirs())
          val oses = roots.map(new File(_, fn)).map(new FileOutputStream(_))
          val buf = new Array[Byte](1024)
          var done = false
          while(!done) {
            val r = dis.read(buf)
            if(r <= 0) done = true
            else oses.foreach(_.write(buf, 0, r))
          }
          oses.foreach(_.close)
        }
      }
      accept
    }
    accept
  }

  def async(body : => Unit) = {
    val t = new Thread(new Runnable {
      override def run(): Unit = {
        body
      }
    })
    t.start()
  }
}


