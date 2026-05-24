package ba.fluxor.fetchapi.feature.tabs.ui.request.raw.format

import org.xml.sax.InputSource
import java.io.StringReader
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object XmlFormatter : RawFormatter {

  private val docBuilderFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance().apply {
    isNamespaceAware = false
    isValidating = false
    setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
    setFeature("http://xml.org/sax/features/external-general-entities", false)
    setFeature("http://xml.org/sax/features/external-parameter-entities", false)
  }

  private val transformerFactory: TransformerFactory = TransformerFactory.newInstance().apply {
    setAttribute("indent-number", 2)
  }

  override fun format(text: String): Result<String> {
    if (text.isBlank()) return Result.success(text)
    return runCatching {
      val builder = docBuilderFactory.newDocumentBuilder()
      val document = builder.parse(InputSource(StringReader(text)))
      document.normalize()

      val transformer = transformerFactory.newTransformer().apply {
        setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        setOutputProperty(OutputKeys.INDENT, "yes")
        setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
      }

      val writer = StringWriter()
      transformer.transform(DOMSource(document), StreamResult(writer))
      writer.toString().trim()
    }
  }

  override fun validate(text: String): Result<Unit> {
    if (text.isBlank()) return Result.success(Unit)
    return runCatching {
      val builder = docBuilderFactory.newDocumentBuilder()
      builder.parse(InputSource(StringReader(text)))
    }.map { }
  }
}
