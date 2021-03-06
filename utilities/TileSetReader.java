import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

/** This class is used for reading tile sets
 * from the XML file created by Tiled.
 * */
public class TileSetReader {

  /** The tile set that the data
   * will be going into. */
  private TileSet tileSet;

  /** Constructs a new instance of a tile set reader.
   * @param tileSet The tile set to put the data into.
   * */
  public TileSetReader(TileSet tileSet) {
    this.tileSet = tileSet;
  }

  /** Opens a file containing a tile set.
   * This function will throw an exception
   * if there is an error in reading the file.
   * @param path The path of the tile to open.
   * */
  public void readFromFile(String path) throws ParserConfigurationException, SAXException, IOException {

    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

    Document doc = docBuilder.parse(new File(path));

    readFromDocument(doc);
  }

  /** Reads the tile set data from an open document.
   * @param doc The document to get the tile set data from.
   * */
  public void readFromDocument(Document doc) throws IOException {

    Element root = doc.getDocumentElement();

    NodeList tileNodes = root.getElementsByTagName("tile");

    for (int i = 0; i < tileNodes.getLength(); i++) {
      Node tileNode = tileNodes.item(i);
      if (tileNode.getNodeType() == Node.ELEMENT_NODE) {
        readTileElement((Element) tileNode);
      }
    }
  }

  /** Reads tile data from a tile element.
   * @param tileElement The element from the XML file containing the tile data.
   * */
  private void readTileElement(Element tileElement) throws IOException {

    Element imageElement = (Element) tileElement.getElementsByTagName("image").item(0);

    NodeList objectGroupNodes = tileElement.getElementsByTagName("objectgroup");

    String id = tileElement.getAttribute("id");

    String imagePath = imageElement.getAttribute("source");

    BufferedImage image = ImageIO.read(new File(imagePath));

    Tile tile = new Tile(Integer.parseInt(id), image);

    for (int i = 0; i < objectGroupNodes.getLength(); i++) {

      Node objectNode = objectGroupNodes.item(i);
      if (objectNode.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }

      readTileObjectGroup(tile, (Element) objectNode);
    }

    tileSet.add(tile);
  }

  /** Reads the element containing the objects of the tile
   * and assigns them as polygons in the tile.
   * @param tile The tile to put the object data into.
   * @param objectGroup The element containing the tile object shapes.
   * */
  private void readTileObjectGroup(Tile tile, Element objectGroup) {

    NodeList objectNodes = objectGroup.getElementsByTagName("object");

    for (int i = 0; i < objectNodes.getLength(); i++) {

      Node objectNode = objectNodes.item(i);

      if (objectNode.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }

      readTileObject(tile, (Element) objectNode);
    }
  }

  /** Reads the object data for a tile.
   * @param tile The tile to put the object data into.
   * @param object The XML object node containing the data points.
   * */
  private void readTileObject(Tile tile, Element objectElement) {
    readTilePolygons(tile, objectElement.getElementsByTagName("polygon"));
  }

  /** Reads the list of polygons found in an object element.
   * @param tile The tile to put the polygon data into.
   * @param polygonNodes The list of polygon nodes from the XML file.
   * */
  private void readTilePolygons(Tile tile, NodeList polygonNodes) {

    for (int i = 0; i < polygonNodes.getLength(); i++) {

      Node polygonNode = polygonNodes.item(i);
      if (polygonNode.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }

      readTilePolygon(tile, (Element) polygonNode);
    }
  }

  /** Reads a polygon element and puts the data into a tile.
   * @param tile The tile to put the data into.
   * @param polygon The polygon element to read the points from.
   * */
  private void readTilePolygon(Tile tile, Element polygon) {
    readTilePolygonPointData(tile, polygon.getAttribute("points"));
  }

  /** Reads a space separated list of polygon points.
   * Each point is separated by a comma. The X value
   * comes first, then the y value follows.
   * @param tile The tile to put the points tino.
   * @param pointData The string containing the point data.
   * */
  private void readTilePolygonPointData(Tile tile, String pointData) {

    Polygon2D polygon = new Polygon2D();

    String[] points = pointData.split(" ");

    for (int i = 0; i < points.length; i++) {

      String[] values = points[i].split(",");

      double x = Double.parseDouble(values[0]);
      double y = Double.parseDouble(values[1]);

      polygon.add(new Vector2D(x, y));
    }

    tile.addPolygon(polygon);
  }
}
