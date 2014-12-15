/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fbconnector;

/**
 *
 * @author Ivan Jelínek
 */
class Settings {
    
    public String getAppToken(){
        return "";
    }   
    
    public int getLimitPages(){
        return 5;
    }

    public String[] getFBPages() {
        String[] array = {
            "denikE15",
            "ceska.piratska.strana",
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
            "novatelevize",
            "iDNES.cz"
        };
        return array;
    }
    
    
}
