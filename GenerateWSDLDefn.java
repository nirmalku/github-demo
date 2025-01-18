import java.io.*;
import java.util.*;
import javax.wsdl.*;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.extensions.schema.Schema;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WSDLToCSVExtractor {

    public static void main(String[] args) {
        // Path to the local WSDL file
        String wsdlFilePath = "C:\\path\\to\\your\\wsdlfile.wsdl"; // Replace with your file path
        String outputCsvPath = "WSDL_Definitions.csv";

        try {
            // Parse the WSDL
            WSDLFactory wsdlFactory = WSDLFactory.newInstance();
            WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
            Definition definition = wsdlReader.readWSDL(wsdlFilePath);

            // Write to CSV file
            try (PrintWriter writer = new PrintWriter(new FileWriter(outputCsvPath))) {
                writer.println("Operation Name,Complex Type Field Name,Simple Type Field Name,Field Description,Data Type");
                processWSDL(definition, writer);
            }

            System.out.println("WSDL definitions written to " + outputCsvPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processWSDL(Definition definition, PrintWriter writer) {
        try {
            for (Object serviceObj : definition.getServices().values()) {
                Service service = (Service) serviceObj;

                for (Object portTypeObj : definition.getPortTypes().values()) {
                    PortType portType = (PortType) portTypeObj;

                    for (Object operationObj : portType.getOperations()) {
                        Operation operation = (Operation) operationObj;
                        String operationName = operation.getName();

                        // Process types
                        Map types = definition.getTypes().getExtensibilityElements();
                        for (Object typeObj : types.values()) {
                            if (typeObj instanceof Schema) {
                                Schema schema = (Schema) typeObj;
                                Element schemaElement = schema.getElement();
                                processSchema(schemaElement, writer, operationName);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processSchema(Element schemaElement, PrintWriter writer, String operationName) {
        NodeList complexTypes = schemaElement.getElementsByTagName("complexType");

        for (int i = 0; i < complexTypes.getLength(); i++) {
            Node complexType = complexTypes.item(i);
            if (complexType instanceof Element) {
                String complexTypeName = ((Element) complexType).getAttribute("name");

                NodeList sequenceNodes = ((Element) complexType).getElementsByTagName("sequence");
                for (int j = 0; j < sequenceNodes.getLength(); j++) {
                    Node sequenceNode = sequenceNodes.item(j);
                    NodeList elementNodes = ((Element) sequenceNode).getElementsByTagName("element");

                    for (int k = 0; k < elementNodes.getLength(); k++) {
                        Node elementNode = elementNodes.item(k);
                        if (elementNode instanceof Element) {
                            String simpleTypeName = ((Element) elementNode).getAttribute("name");
                            String dataType = ((Element) elementNode).getAttribute("type");
                            String description = ((Element) elementNode).getAttribute("description");

                            // Write to CSV
                            writer.printf("%s,%s,%s,%s,%s%n",
                                    operationName,
                                    complexTypeName,
                                    simpleTypeName,
                                    description != null ? description : "N/A",
                                    dataType);
                        }
                    }
                }
            }
        }
    }
}
