package example.models

case class Color(r: Int, g: Int, b: Int) {
  def hex: String = s"#${r.toHexString}${g.toHexString}${b.toHexString}"
}
