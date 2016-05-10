package massim.eismassim;

import eis.iilang.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import java.util.*;

/**
 * Created by Tobias Ahlbrecht on 09.04.15.
 */
public class City2015Entity extends Entity{

    private String role = "unknown";

    /**
     * Instantiates an entity. Supposed to be only
     * called by a factory.
     */
    protected City2015Entity() {
        super();
    }

    @Override
    public String getType() {
        return "city2015entity"+this.role;
    }

    @Override
    protected Document actionToXML(Action action) {

        String actionType = null;
        actionType = action.getName();

        String actionParameter = null;
        if ( action.getParameters().size() != 0)
            actionParameter = action.getParameters().element().toProlog();

        String actionId = null;
        actionId = "" + getCurrentActionId();

        // create document
        Document doc = null;
        try {

            doc = documentbuilderfactory.newDocumentBuilder().newDocument();
            Element root = doc.createElement("message");
            root.setAttribute("type","action");
            doc.appendChild(root);

            Element auth = doc.createElement("action");
            auth.setAttribute("type",actionType);
            if ( actionParameter != null )
                auth.setAttribute("param",actionParameter);
            auth.setAttribute("id",actionId);
            root.appendChild(auth);

        } catch (ParserConfigurationException e) {
            System.err.println("unable to create new document");
            return null;
        }

        //System.out.println(action.toProlog());
        //System.out.println(XMLToString(doc));

        return doc;

    }

    @Override
    protected Collection<Percept> byeToIIL(Document document) {
        assert false : XMLToString(document);
        return null;
    }

    @Override
    protected Collection<Percept> requestActionToIIL(Document document) {

        /*
         * Parsing of MapSimulationAgentPerception
         */

        HashSet<Percept> ret = new HashSet<Percept>();

        // parse the <message>-tag
        Element root = document.getDocumentElement();
        assert root != null;
        assert root.getAttribute("type").equalsIgnoreCase("request-action");

        long timestamp = new Long(fromXML(root, "timestamp")).longValue();
        ret.add(new Percept("timestamp", new Numeral(timestamp)));

        // parse the <perception>-tag
        Element perception = (Element)root.getElementsByTagName("perception").item(0);
        assert perception.getNodeName().equalsIgnoreCase("perception");

        long deadline = new Long(fromXML(perception, "deadline")).longValue();
        ret.add(new Percept("deadline",new Numeral(deadline)));

        // used later
        NodeList tags;
        Element tag;

        // <simulation>-tag
        tags = perception.getElementsByTagName("simulation");
        assert tags.getLength() == 1 : XMLToString(document);
        tag = (Element) tags.item(0);

        ret.add(new Percept("step", new Numeral(new Integer(fromXML(tag, "step")))));

        // <self>-tag
        tags = perception.getElementsByTagName("self");
        assert tags.getLength() == 1;
        tag = (Element) tags.item(0);

        ret.add(new Percept("charge", new Numeral(new Integer(fromXML(tag, "charge")))));
        ret.add(new Percept("load", new Numeral(new Integer(fromXML(tag, "load")))));

        ret.add(new Percept("lastAction", new Identifier(fromXMLWithEmpty(tag, "lastAction"))));
        ret.add(new Percept("lastActionParam", new Identifier(fromXMLWithEmpty(tag, "lastActionParam"))));
        ret.add(new Percept("lastActionResult", new Identifier(fromXMLWithEmpty(tag, "lastActionResult"))));

//        ret.add(new Percept("batteryCapacity", new Numeral(new Integer(fromXML(tag, "batteryCapacity")))));
//        ret.add(new Percept("loadCapacity", new Numeral(new Integer(fromXML(tag, "loadCapacity")))));
        ret.add(new Percept("lat", new Numeral(new Double(fromXML(tag, "lat")))));
        ret.add(new Percept("lon", new Numeral(new Double(fromXML(tag, "lon")))));
        ret.add(new Percept("inFacility", new Identifier(fromXMLWithEmpty(tag, "inFacility"))));
        ret.add(new Percept("fPosition", new Numeral(new Integer(fromXML(tag, "fPosition")))));
        ret.add(new Percept("routeLength", new Numeral(new Integer(fromXML(tag, "routeLength")))));

        NodeList items = tag.getElementsByTagName("items");
        assert items.getLength() == 1;
        NodeList item = ((Element)items.item(0)).getElementsByTagName("item");
        for(int i = 0; i < item.getLength(); i++){
            Element it = (Element) item.item(i);
            ret.add(new Percept("item", new Identifier(fromXML(it, "name")), new Numeral(new Integer(fromXML(it, "amount"))) ));
        }

        NodeList wps = tag.getElementsByTagName("n");
        ParameterList waypoints = new ParameterList();
        for(int i = 0; i < wps.getLength(); i++){
            Element wp = (Element) wps.item(i);
            int id = Integer.parseInt(fromXML(wp, "i"));
            double lat = Double.parseDouble(fromXML(wp, "lat"));
            double lon = Double.parseDouble(fromXML(wp, "lon"));

            //ret.add(new Percept("wp", new Numeral(id), new Numeral(lat), new Numeral(lon)));

            waypoints.add(new Function("wp", new Numeral(id), new Numeral(lat), new Numeral(lon)));
        }
        if(!waypoints.isEmpty()){
            ret.add(new Percept("route", waypoints));
        }

        // <team>-tag
        tags = perception.getElementsByTagName("team");
        assert tags.getLength() == 1;
        tag = (Element) tags.item(0);

        ret.add(new Percept("money", new Numeral(Long.parseLong(fromXML(tag, "money")))));
        //ret.add(xmlToNumericPercept(tag, "rank"));

        tags = tag.getElementsByTagName("jobs-taken");
        assert tags.getLength() == 1;
        tag = (Element) tags.item(0);
        tags = tag.getElementsByTagName("job");
        for(int i = 0; i<tags.getLength(); i++){
            Element job = (Element) tags.item(i);
            ret.add(new Percept("jobTaken", new Identifier(fromXML(job, "id"))));
        }

        tags = ((Element)perception.getElementsByTagName("team").item(0)).getElementsByTagName("jobs-posted");
        assert tags.getLength() == 1;
        tag = (Element) tags.item(0);
        tags = tag.getElementsByTagName("job");
        for(int i = 0; i<tags.getLength(); i++){
            Element job = (Element) tags.item(i);
            ret.add(new Percept("jobPosted", new Identifier(fromXML(job, "id"))));
        }

        tags = perception.getElementsByTagName("entities");
        assert tags.getLength() == 1;
        tag = (Element)tags.item(0);
        tags = tag.getElementsByTagName("entity");
        for(int i = 0; i < tags.getLength(); i++){
            tag = (Element)tags.item(i);
            String name = fromXML(tag, "name");
            String team = fromXML(tag, "team");
            double lat = Double.parseDouble(fromXML(tag, "lat"));
            double lon = Double.parseDouble(fromXML(tag, "lon"));
            String role = fromXML(tag, "role");
            ret.add(new Percept("entity", new Identifier(name), new Identifier(team), new Numeral(lat), new Numeral(lon), new Identifier(role)));
        }

        tags = perception.getElementsByTagName("facilities");
        assert tags.getLength() == 1;
        tag = (Element)tags.item(0);
        NodeList iTags = tag.getElementsByTagName("chargingStation");
        for(int i = 0; i < iTags.getLength(); i++){
            Element iTag = (Element)iTags.item(i);
            LinkedList<Parameter> params = new LinkedList<>();
            params.add( new Identifier(fromXML(iTag, "name")) );
            params.add(new Numeral(Double.parseDouble(fromXML(iTag, "lat"))));
            params.add( new Numeral(Double.parseDouble(fromXML(iTag, "lon"))) );
            params.add( new Numeral(Integer.parseInt(fromXML(iTag, "rate"))));
            params.add( new Numeral(Integer.parseInt(fromXML(iTag, "price"))));
            params.add( new Numeral(Integer.parseInt(fromXML(iTag, "slots"))));

            NodeList infTags =  iTag.getElementsByTagName("info");
            boolean visible = false;
            if(infTags.getLength() > 0){
                assert infTags.getLength() == 1;
                iTag = (Element) infTags.item(0);
                params.add(new Identifier(fromXML(iTag, "qSize")));
                visible = true;
            }

            if(visible) {
                ret.add(new Percept("visibleChargingStation", params));
            }
            else{
                ret.add(new Percept("chargingStation", params));
            }
        }

        iTags = tag.getElementsByTagName("dumpLocation");
        for(int i = 0; i < iTags.getLength(); i++){
            Element iTag = (Element)iTags.item(i);
            LinkedList<Parameter> params = new LinkedList<>();
            params.add( new Identifier(fromXML(iTag, "name")) );
            params.add( new Numeral(Double.parseDouble(fromXML(iTag, "lat"))) );
            params.add( new Numeral(Double.parseDouble(fromXML(iTag, "lon"))) );
            params.add( new Numeral(Integer.parseInt(fromXML(iTag, "price"))));

            ret.add(new Percept("dump", params));
        }

        iTags = tag.getElementsByTagName("shop");
        for(int i = 0; i < iTags.getLength(); i++){
            Element iTag = (Element)iTags.item(i);
            LinkedList<Parameter> params = new LinkedList<>();
            String shopName = fromXML(iTag, "name");
            params.add( new Identifier(shopName) );
            params.add( new Numeral(Double.parseDouble(fromXML(iTag, "lat"))) );
            params.add( new Numeral(Double.parseDouble(fromXML(iTag, "lon"))) );

            ParameterList parItems = new ParameterList();
            params.add(parItems);

            NodeList itemTags = iTag.getElementsByTagName("item");
            for(int j = 0; j < itemTags.getLength(); j++){
                Element itemTag = (Element)itemTags.item(j);
                LinkedList<Parameter> itemParams = new LinkedList<>();
                Function fItem = new Function("item");
                itemParams.add(new Identifier(fromXML(itemTag, "name")));

                NodeList infoTags = itemTag.getElementsByTagName("info");

                if(infoTags.getLength() > 0){
                    assert infoTags.getLength() == 1;

                    Element infoTag = (Element) infoTags.item(0);
                    itemParams.add(new Numeral(Integer.parseInt(fromXML(infoTag, "cost"))));
                    itemParams.add(new Numeral(Integer.parseInt(fromXML(infoTag, "amount"))));
                    itemParams.add(new Numeral(Integer.parseInt(fromXML(infoTag, "restock"))));

                    fItem.setName("availableItem");
                }

                //fill function and add to ParamList
                fItem.setParameters(itemParams);
                parItems.add(fItem);
            }

            ret.add(new Percept("shop", params));
        }

        //parse storage
        iTags = tag.getElementsByTagName("storage");
        for(int i = 0; i < iTags.getLength(); i++){
            Element iTag = (Element)iTags.item(i);

            LinkedList<Parameter> params = new LinkedList<>();
            params.add( new Identifier(fromXML(iTag, "name")) );
            params.add( new Numeral(Double.parseDouble(fromXML(iTag, "lat"))) );
            params.add( new Numeral(Double.parseDouble(fromXML(iTag, "lon"))) );
            params.add( new Numeral(Integer.parseInt(fromXML(iTag, "price"))) );
            params.add( new Numeral(Integer.parseInt(fromXML(iTag, "totalCapacity"))) );
            params.add( new Numeral(Integer.parseInt(fromXML(iTag, "usedCapacity"))) );

            //create parameterlist for items
            ParameterList parItems = new ParameterList();
            params.add(parItems);

            NodeList itemTags = iTag.getElementsByTagName("item");
            for(int j = 0; j < itemTags.getLength(); j++){
                Element itemTag = (Element) itemTags.item(j);
                LinkedList<Parameter> itemParams = new LinkedList<>();

                //add one function per item to parameterlist
                Function fItem = new Function("item");

                itemParams.add(new Identifier(fromXML(itemTag, "name")));
                itemParams.add(new Numeral(Integer.parseInt(fromXML(itemTag, "stored"))));
                itemParams.add(new Numeral(Integer.parseInt(fromXML(itemTag, "delivered"))));

                fItem.setParameters(itemParams);

                parItems.add(fItem);
            }

            ret.add(new Percept("storage", params));
        }

        iTags = tag.getElementsByTagName("workshop");
        for(int i = 0; i < iTags.getLength(); i++) {
            Element iTag = (Element) iTags.item(i);
            ret.add(new Percept("workshop",
                        new Identifier(fromXML(iTag, "name")),
                        new Numeral(Double.parseDouble(fromXML(iTag, "lat"))),
                        new Numeral(Double.parseDouble(fromXML(iTag, "lon"))),
                        new Numeral(Integer.parseInt(fromXML(iTag, "price")))
                    ));
        }

        //parse jobs
        tags = perception.getElementsByTagName("jobs");
        assert tags.getLength() == 1;
        tag = (Element)tags.item(0);

        iTags = tag.getElementsByTagName("auctionJob");
        for(int i = 0; i < iTags.getLength(); i++){
            Element iTag = (Element)iTags.item(i);

            LinkedList<Parameter> params = new LinkedList<>();
            params.add( new Identifier(fromXML(iTag, "id")) );
            params.add( new Identifier(fromXML(iTag, "storage")) );
            params.add( new Numeral(Integer.parseInt(fromXML(iTag, "begin"))) );
            params.add( new Numeral(Integer.parseInt(fromXML(iTag, "end"))) );
            params.add( new Numeral(Integer.parseInt(fromXML(iTag, "fine"))) );
            params.add( new Numeral(Integer.parseInt(fromXML(iTag, "maxBid"))) );

//            NodeList resultTags = iTag.getElementsByTagName("result");
//            if(resultTags.getLength() == 1){
//                Element resTag = (Element)resultTags.item(0);
//                params.add(new Function("winner", new Identifier(fromXML(resTag, "winner"))));
//            }

            //create parameterlist for items
            ParameterList parItems = new ParameterList();
            params.add(parItems);

            NodeList itemTags = iTag.getElementsByTagName("item");
            for(int j = 0; j < itemTags.getLength(); j++){
                Element itemTag = (Element) itemTags.item(j);
                LinkedList<Parameter> itemParams = new LinkedList<>();

                //add one function per item to parameterlist
                Function fItem = new Function("item");

                itemParams.add(new Identifier(fromXML(itemTag, "name")));
                itemParams.add(new Numeral(Integer.parseInt(fromXML(itemTag, "amount"))));

                fItem.setParameters(itemParams);

                parItems.add(fItem);
            }

            ret.add(new Percept("auctionJob", params));
        }

        iTags = tag.getElementsByTagName("pricedJob");
        for(int i = 0; i < iTags.getLength(); i++){
            Element iTag = (Element)iTags.item(i);

            LinkedList<Parameter> params = new LinkedList<>();
            params.add( new Identifier(fromXML(iTag, "id")) );
            params.add( new Identifier(fromXML(iTag, "storage")) );
            params.add( new Numeral(Integer.parseInt(fromXML(iTag, "begin"))) );
            params.add( new Numeral(Integer.parseInt(fromXML(iTag, "end"))) );
            params.add( new Numeral(Integer.parseInt(fromXML(iTag, "reward"))) );

            //create parameterlist for items
            ParameterList parItems = new ParameterList();
            params.add(parItems);

            NodeList itemTags = iTag.getElementsByTagName("item");
            for(int j = 0; j < itemTags.getLength(); j++){
                Element itemTag = (Element) itemTags.item(j);
                LinkedList<Parameter> itemParams = new LinkedList<>();

                //add one function per item to parameterlist
                Function fItem = new Function("item");

                itemParams.add(new Identifier(fromXML(itemTag, "name")));
                itemParams.add(new Numeral(Integer.parseInt(fromXML(itemTag, "amount"))));

                String del = itemTag.getAttribute("delivered");
                if(del != null && !del.equals("") && !del.equals("null")){
                    itemParams.add(new Numeral(Integer.parseInt(del)));
                }

                fItem.setParameters(itemParams);

                parItems.add(fItem);
            }

            ret.add(new Percept("pricedJob", params));
        }

        return ret;
    }

    /**
     * Reads key out of the given Element; checks for null and empty String
     * @param tag the Element to search under
     * @param key the key to search for
     * @return the value that was found
     */
    private static String fromXML(Element tag, String key){
        String str = tag.getAttribute(key);
        assert str != null && !str.equals(""): XMLToString(tag);
        return str;
    }

    private static String fromXMLWithEmpty(Element tag, String key){
        String str = tag.getAttribute(key);
        assert str != null : XMLToString(tag);
        if(str.equals("") || str.equals("null")) str = "none";
        return str;
    }

    @Override
    protected Collection<Percept> simEndToIIL(Document document) {

        // <?xml version="1.0" encoding="UTF-8" standalone="no"?><message timestamp="1296546945647" type="sim-end">
        // <sim-result ranking="2" score="35"/>
        // </message>

        HashSet<Percept> ret = new HashSet<Percept>();

        String str;

        // parse the <message>-tag
        Element root = document.getDocumentElement();
        assert root != null;
        assert root.getAttribute("type").equalsIgnoreCase("sim-end");

        // parse the <simulation>-tag
        Element simResult = (Element)root.getElementsByTagName("sim-result").item(0);
        assert simResult.getNodeName().equalsIgnoreCase("sim-result");
        str = simResult.getAttribute("ranking");
        assert str != null && str.equals("") == false;
        ret.add(new Percept("ranking",new Numeral(new Integer(str).intValue())));
        str = simResult.getAttribute("score");
        assert str != null && str.equals("") == false;
        ret.add(new Percept("score",new Numeral(new Integer(str).intValue())));

        return ret;

    }

    @Override
    protected Collection<Percept> simStartToIIL(Document document) {

        HashSet<Percept> ret = new HashSet<Percept>();

        //<?xml version="1.0" encoding="UTF-8" standalone="no"?><message timestamp="1296226870974" type="sim-start">
        //<simulation edges="46" id="0" steps="1000" vertices="20"/>

        // parse the <message>-tag
        Element root = document.getDocumentElement();
        assert root != null;
        assert root.getAttribute("type").equalsIgnoreCase("sim-start");

        // parse the <simulation>-tag
        Element simulation = (Element)root.getElementsByTagName("simulation").item(0);
        assert simulation.getNodeName().equalsIgnoreCase("simulation");

        ret.add(new Percept("id", new Identifier(fromXML(simulation, "id"))));
        ret.add(new Percept("map", new Identifier(fromXML(simulation, "map"))));
        ret.add(new Percept("seedCapital", new Numeral(new Long(fromXML(simulation, "seedCapital")).longValue())));
        ret.add(new Percept("steps", new Numeral(new Integer(fromXML(simulation, "steps")).intValue())));
        ret.add(new Percept("team", new Identifier(fromXML(simulation, "team"))));

        NodeList roleTags = simulation.getElementsByTagName("role");
        assert roleTags.getLength() == 1;
        Element roleTag = (Element) roleTags.item(0);

        NodeList toolTags = roleTag.getElementsByTagName("tool");

        ParameterList p = new ParameterList();

        for(int i = 0; i < toolTags.getLength(); i++){
            p.add( new Identifier(fromXML((Element) toolTags.item(i), "name")));
        }

        ret.add(new Percept("role",
                new Identifier(fromXML(roleTag, "name")),
                new Numeral(new Integer(fromXML(roleTag, "speed")).intValue()),
                new Numeral(new Integer(fromXML(roleTag, "maxLoad")).intValue()),
                new Numeral(new Integer(fromXML(roleTag, "maxBattery")).intValue()),
                p
        ));

        NodeList products = simulation.getElementsByTagName("product");
        for(int i = 0; i < products.getLength(); i++){
            Element product = (Element) products.item(i);
            LinkedList<Parameter> params = new LinkedList<>();
            params.add(new Identifier(fromXML(product, "name")));
            params.add(new Numeral(new Integer(fromXML(product, "volume")).intValue()));

            ParameterList reqs = new ParameterList();
            params.add(reqs);

            for(String s: new String[]{"consumed", "tools"}){
                NodeList nodes = product.getElementsByTagName(s);
                if(nodes.getLength() == 1){
                    nodes = ((Element)nodes.item(0)).getElementsByTagName("item");
                    for(int j = 0; j < nodes.getLength(); j++){
                        Element item = (Element) nodes.item(j);
                        reqs.add(new Function(s,
                                new Identifier(fromXML(item, "name")),
                                new Numeral(Integer.parseInt(fromXML(item, "amount")))));
                    }
                }
            }

            ret.add(new Percept("product", params));
        }

        return ret;
    }

    public void setRole(String str) {
        this.role = str;
    }
}