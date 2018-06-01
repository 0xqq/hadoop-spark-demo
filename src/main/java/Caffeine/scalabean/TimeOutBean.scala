package Caffeine.scalabean

case class TimeOutBean(
                        val api_uri: String, //请求地址
                        val kpi_id: Int, //指标编码
                        val service_code: String, //业务系统编码
                        val timeout_time: Int //超时时间
                      ) extends Product with Serializable {

  override def productArity: Int = 4

  override def canEqual(that: Any): Boolean = that.isInstanceOf[TimeOutBean]

  override def productElement(n: Int): Any = n match {
    case 0 => api_uri
    case 1 => kpi_id
    case 2 => service_code
    case 3 => timeout_time
  }
}

object TimeOutBean {
  def buidEmpth(): TimeOutBean = {
    new TimeOutBean("", 0, "", 0)
  }
}
