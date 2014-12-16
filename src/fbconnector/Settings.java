/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fbconnector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

/**
 *
 * @author Ivan Jelínek
 */
class Settings {

    private String configURL = "config.txt";
    private String ipES;
    private TreeMap<String, String> segmentMap = new TreeMap();
    private String portES;
    private String indexES;
    private String typeES;
    private ArrayList<String> targetList = new ArrayList<>();
    private int pageLimit;
    private String appToken = "1441688612771153|DFRtjsw4UuFbHyilWQJ3BK4ftc0";
    private Settings thisSetting = null;

    public Settings() {
        loadConfig();
    }

    public Settings getThisSettings() {
        return this.thisSetting;
    }

    public String getAppToken() {
        return this.appToken;
    }

    public int getLimitPages() {
        return this.pageLimit;
    }

    public String getIpES() {
        return ipES;
    }

    public String getPortES() {
        return portES;
    }

    public String getIndexES() {
        return indexES;
    }

    public String getTypeES() {
        return typeES;
    }

    public int getPageLimit() {
        return pageLimit;
    }

    public String getSegmentForPage(String pageName) {
        for (String key : segmentMap.keySet()) {
            if (pageName.equalsIgnoreCase(key)) {
                return segmentMap.get(key);
            }
        }
        return "default";
    }

    public String[] getFBPages() {

        String[] array = new String[targetList.size()];
        int i = 0;
        for (String s : this.targetList) {
            array[i] = targetList.get(i);
            i++;
        }

        /* String[] array = {
         "Burinka.stavebni.sporitelna",
         "RaiffeisenbankCZ",
         "gemoney.cz",
         "Airbank",
         "zuno.cz",
         "mBank.cz",
         "equabank",
         "homecredit",
         "ods.cz",
         "cssdcz",
         "kducsl",
         "hospodarskenovinycz",
         "xportprague",
         "tydenikeuro",
         "DATART.cz",
         "ceskasporitelna",
         "Ceskaposta",
         "komercni.banka",
         "ceskatelevize",
         "ihned.cz",
         "denikE15",
         "ceska.piratska.strana",
         "novatelevize",
         "iDNES.cz"
         };*/
        return array;
    }

    private void loadConfig() {
        BufferedReader br = null;
        System.out.println(new Date() + " Loading config.");
        try {
            br = new BufferedReader(new FileReader(this.configURL));
            String line = br.readLine();
            String[] pole;

            while (line != null) {
                /*    if (line.toLowerCase().contains("[license]")) {
                 while (!line.toLowerCase().contains("[server]")) {
                 line = br.readLine();
                 pole = line.split(":");
                 if (pole[0].equals("licensekey")) {
                 //licensekey:veovber
                 License.checkLicense(pole[1]);
                 }
                 }
                 }*/
                if (line.toLowerCase().contains("[server]")) {
                    while (!line.toLowerCase().contains("[task]")) {
                        line = br.readLine();
                        if (!line.startsWith("//")) { //možnost komentovat v config.txt - začátek řádku je //
                            pole = line.split(":");
                            if (pole[0].equals("ip")) {
                                //ip:192.168.0.0
                                this.ipES = pole[1];
                            }
                            if (pole[0].equals("port")) {
                                this.portES = pole[1];
                            }
                            if (pole[0].equals("appToken")) {
                                this.appToken = pole[1];
                            }
                            if (pole[0].equals("pageLimit")) {
                                this.pageLimit = Integer.parseInt(pole[1]);
                            }
                            if (pole[0].equals("index")) {
                                this.indexES = pole[1];
                            }
                            if (pole[0].equals("type")) {
                                this.typeES = pole[1];
                            }
                        }
                    }
                }
                if (line.toLowerCase().contains("[task]")) {
                    // line = br.readLine();
                    while (!line.toLowerCase().contains("[end]")) {
                        if (!line.startsWith("//")) { //možnost komentovat v config.txt - začátek řádku je //
                            pole = line.split(":");
                            if (pole[0].equals("download")) {
                                //ip:192.168.0.0
                                this.targetList.add(pole[1]);
                                this.segmentMap.put(pole[1], pole[3]);
                            }

                        }
                        line = br.readLine();
                        if (line.contains("[end]")) {
                            break;
                        }

                    }
                }
                if (line.contains("[end]")) {
                    break;
                }
                line = br.readLine();
            }
        } catch (IOException ex) {
            System.out.println(new Date() + " IO Exception in reading config.");
        }
        System.out.println(new Date() + " Config loaded.");
    }

}
