package kent.group8.senseplateandroid;

public class Signalton {
    private static Signalton signal = new Signalton();
    private String weight;
    private String moi;
    private String temp;
    private String pic;

    private Signalton(){
        weight = "100";
        pic = "";
        moi = "0.98Aw";
        temp = "25˚";
    }


    public String getWeight() {
        return weight;
    }

    public String getMoi() { return moi; }

    public String getTemp() { return temp; }

    public String getPic() { return  pic;}

    public void addPic(String pic){ this.pic = pic;}

    public void addWeight(String weight){
        this.weight = weight;
    }

    public void addMoi(String moi) { this.moi = moi;}

    public void addTemp(String temp){ this.temp = temp;}

    public static Signalton getInstance(){ return signal;}
}
