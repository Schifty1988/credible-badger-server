package com.crediblebadger.travel;

import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class TravelServiceTest {

    @Test
    @Disabled
    public void testProcessArray() {
        String[] places = {
            "Berlin","Hamburg","Munich","Cologne","Frankfurt am Main","Stuttgart","Düsseldorf","Dortmund","Essen","Leipzig","Bremen","Dresden","Hanover","Nuremberg","Duisburg","Bochum","Wuppertal","Bonn","Mannheim","Karlsruhe","Augsburg","Wiesbaden","Gelsenkirchen","Mönchengladbach","Mainz","Herne","Marl","Oberhausen","Lübeck","Oldenburg","Solingen","Hagen","Magdeburg","Kiel","Halle","Recklinghausen","Freiburg","Erfurt","Remscheid","Pforzheim","Saarbrücken","Ingolstadt","Hanau","Offenbach am Main","Fürth","Landshut","Heidelberg","Bad Homburg","Göttingen","Regensburg",
            "New York","New York City","Los Angeles","Chicago","Houston","Philadelphia","Phoenix","San Antonio","San Diego","Dallas","San Jose","Jacksonville","San Francisco","Indianapolis","Austin","Columbus","Fort Worth","Charlotte","Seattle","Denver","Washington D.C.","Boston","El Paso","Nashville","Detroit","Oklahoma City","Las Vegas","Memphis","Louisville","Baltimore","Milwaukee","Albuquerque","Tucson","Fresno","Sacramento","Kansas City","Mesa","Virginia Beach","Atlanta","Colorado Springs","Omaha","Raleigh","Miami","Cleveland","Tampa","St. Louis","Minneapolis","Bakersfield","Honolulu","Arlington","Stockholm","Oslo","Helsinki","Copenhagen","Rome","Milan","Florence","Venice","Paris","Marseille","Barcelona","Madrid","Seville","Valencia","Berlin","Munich","Frankfurt","Amsterdam","Rotterdam","Hague","London","Manchester","Birmingham","Edinburgh","Glasgow","Beijing","Shanghai","Hong Kong","Tokyo","Osaka","Yokohama","Seoul","Busan","Jakarta","Bandung","Manila","Quezon City","Ho Chi Minh City","Saigon","Bangkok","Delhi","Mumbai","Kolkata","Chennai","Hyderabad","Singapore","Kuala Lumpur","Jakarta","Sydney","Melbourne","Brisbane","Auckland","Wellington","Johannesburg","Cape Town","Nairobi","Lagos","Abuja","Rio de Janeiro","Sao Paulo","Buenos Aires","Santiago","Montevideo","Caracas","Lima","Quito","La Paz","Addis Ababa","Nairobi","Dubai","Abu Dhabi","Doha","Kuwait City","Riyadh",
            "Afghanistan","Albania","Algeria","Andorra","Angola","Antigua and Barbuda","Argentina","Armenia","Australia","Austria","Azerbaijan","Bahamas","Bahrain","Bangladesh","Barbados","Belarus","Belgium","Belize","Benin","Bhutan","Bolivia","Bosnia and Herzegovina","Botswana","Brazil","Brunei","Bulgaria","Burkina Faso","Burundi","Cabo Verde","Cambodia","Cameroon","Canada","Central African Republic","Chad","Chile","China","Colombia","Comoros","Congo","Costa Rica","Cote d'Ivoire","Croatia","Cuba","Cyprus","Czech Republic","Denmark","Djibouti","Dominica","Dominican Republic","Ecuador","Egypt","El Salvador","Equatorial Guinea","Eritrea","Estonia","Eswatini","Ethiopia","Fiji","Finland","France","Gabon","Gambia","Georgia","Germany","Ghana","Greece","Grenada","Guatemala","Guinea","Guinea-Bissau","Guyana","Haiti","Honduras","Hungary","Iceland","India","Indonesia","Iran","Iraq","Ireland","Israel","Italy","Jamaica","Japan","Jordan","Kazakhstan","Kenya","Kiribati","Kuwait","Kyrgyzstan","Laos","Latvia","Lebanon","Lesotho","Liberia","Libya","Liechtenstein","Lithuania","Luxembourg","Madagascar","Malawi","Malaysia","Maldives","Mali","Malta","Marshall Islands","Mauritania","Mauritius","Mexico","Micronesia","Moldova","Monaco","Mongolia","Montenegro","Morocco","Mozambique","Myanmar","Namibia","Nauru","Nepal","Netherlands","New Zealand","Nicaragua","Niger","Nigeria","North Korea","North Macedonia","Norway","Oman","Pakistan","Palau","Palestine","Panama","Papua New Guinea","Paraguay","Peru","Philippines","Poland","Portugal","Qatar","Romania","Russia","Rwanda","Saint Kitts and Nevis","Saint Lucia","Saint Vincent and the Grenadines","Samoa","San Marino","Sao Tome and Principe","Saudi Arabia","Senegal","Serbia","Seychelles","Sierra Leone","Singapore","Slovakia","Slovenia","Solomon Islands","Somalia","South Africa","South Korea","South Sudan","Spain","Sri Lanka","Sudan","Suriname","Sweden","Switzerland","Syria","Taiwan","Tajikistan","Tanzania","Thailand","Timor-Leste","Togo","Tonga","Trinidad and Tobago","Tunisia","Turkey","Turkmenistan","Tuvalu","Uganda","Ukraine","United Arab Emirates","United Kingdom","United States","Uruguay","Uzbekistan","Vanuatu","Vatican City","Venezuela","Vietnam","Yemen","Zambia","Zimbabwe", 
            "Kabul","Tirana","Algiers","Andorra la Vella","Luanda","St. John's","Buenos Aires","Yerevan","Canberra","Vienna","Baku","Nassau","Manama","Dhaka","Bridgetown","Minsk","Brussels","Belmopan","Porto-Novo","Thimphu","Sucre","Sarajevo","Gaborone","Brasília","Bandar Seri Begawan","Sofia","Ouagadougou","Gitega","Praia","Phnom Penh","Yaoundé","Ottawa","Bangui","N'Djamena","Santiago","Beijing","Bogotá","Moroni","Brazzaville","Kinshasa","San José","Yamoussoukro","Zagreb","Havana","Nicosia","Prague","Copenhagen","Djibouti","Roseau","Santo Domingo","Quito","Cairo","San Salvador","Malabo","Asmara","Tallinn","Mbabane","Addis Ababa","Suva","Helsinki","Paris","Libreville","Banjul","Tbilisi","Berlin","Accra","Athens","St. George's","Guatemala City","Conakry","Bissau","Georgetown","Port-au-Prince","Tegucigalpa","Budapest","Reykjavik","New Delhi","Jakarta","Tehran","Baghdad","Dublin","Jerusalem","Rome","Kingston","Tokyo","Amman","Astana","Nairobi","South Tarawa","Kuwait City","Bishkek","Vientiane","Riga","Beirut","Maseru","Monrovia","Tripoli","Vaduz","Vilnius","Luxembourg","Antananarivo","Lilongwe","Kuala Lumpur","Malé","Bamako","Valletta","Majuro","Nouakchott","Port Louis","Mexico City","Palikir","Chisinau","Monaco","Ulaanbaatar","Podgorica","Rabat","Maputo","Naypyidaw","Windhoek","Yaren","Kathmandu","Amsterdam","Wellington","Managua","Niamey","Abuja","Pyongyang","Skopje","Oslo","Muscat","Islamabad","Ngerulmud","Jerusalem","Panama City","Port Moresby","Asunción","Lima","Manila","Warsaw","Lisbon","Doha","Bucharest","Moscow","Kigali","Basseterre","Castries","Kingstown","Apia","San Marino","São Tomé","Riyadh","Dakar","Belgrade","Victoria","Freetown","Singapore","Bratislava","Ljubljana","Honiara","Mogadishu","Pretoria","Seoul","Juba","Madrid","Colombo","Khartoum","Paramaribo","Stockholm","Bern","Damascus","Taipei","Dushanbe","Dodoma","Bangkok","Dili","Lomé","Nuku'alofa","Port of Spain","Tunis","Ankara","Ashgabat","Funafuti","Kampala","Kyiv","Abu Dhabi","London","Washington D.C.","Montevideo","Tashkent","Port Vila","Vatican City","Caracas","Hanoi","Sanaa","Lusaka","Harare"
       };
        
        System.out.println("Number of Places: " + places.length);

        RestTemplate restTemplate = new RestTemplate();

        // Create the headers and set the content type to JSON
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        TravelGuideRequest tgr = new TravelGuideRequest();
        tgr.setChildFriendly(true);
        HttpEntity<TravelGuideRequest> request = new HttpEntity<>(tgr, headers);

        List<String> failed = new LinkedList();
        
        for (String currentPlace : places) {
            tgr.setPlace(currentPlace);

            try {
                ResponseEntity<String> response = restTemplate.exchange(
                        createURLWithPort("/api/travel/travelGuide"),
                        HttpMethod.POST,
                        request,
                        String.class);
                
                System.out.println(currentPlace + ": " + response.getStatusCode());
                if (!response.getStatusCode().is2xxSuccessful()) {
                    failed.add(currentPlace);
                }
                
            } catch (Exception e) {
                System.out.println("Status Code: " + e);
                failed.add(currentPlace);
            }
        }
        
        System.out.println("Number of failed Places: " + failed.size());
        
        for (String currentPlace : failed) {
            System.out.println(currentPlace);
        }
    }

    private String createURLWithPort(String uri) {
        return "https://crediblebadger.com" + uri;
    }
}
