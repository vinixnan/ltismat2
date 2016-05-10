package massim.competition2015.monitor.data;

/**
 * Created by ta10 on 24.03.15.
 */
public class FacilityInfo {

    public String name;
    public String type;
    public double lat;
    public double lon;

    public String toString(){return this.name;}

    public FacilityExtra extra;
	


    /*
     * Additional inner classes
     */

    public static class FacilityExtra{}

    public static class DumpLocationInfo extends FacilityExtra{
        public int price;
    }

    public static class ChargingStationInfo extends FacilityExtra{
        public int fuelPrice;
        public int rate;
        public int qSize;
        public int maxConcurrent;
    }

    public static class ShopInfo extends FacilityExtra{
        public String stock;
    }

    public static class StorageInfo extends FacilityExtra{
        public String capacity;
        public int price;
        public String stored;
    }

    public static class WorkshopInfo extends FacilityExtra{
        public int price;
    }
}
