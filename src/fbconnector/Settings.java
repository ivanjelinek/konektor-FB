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
        return "XXX";
    }   

    public String[] getFBPages() {
        String[] array = {
            "ceskasporitelna",
            "Ceskaposta", 
            "komercni.banka", 
            "ceskatelevize", 
            "ihned.cz",
            "novatelevize",
            "iDNES.cz",
            "ceska.piratska.strana",
            "ods.cz",
            "cssdcz",
            "kducsl"};
        return array;
    }
    
    
}
