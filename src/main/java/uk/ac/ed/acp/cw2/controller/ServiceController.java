package uk.ac.ed.acp.cw2.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.lang.Math.*;

/**
 * Controller class that handles various HTTP endpoints for the application.
 * Provides functionality for serving the index page, retrieving a static UUID,
 * and managing key-value pairs through POST requests.
 */
@RestController()
@RequestMapping("/api/v1")
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @Value("${ilp.service.url}")
    public URL serviceUrl;


    @GetMapping("/")
    public String index() {
        return "<html><body>" +
                "<h1>Welcome from ILP</h1>" +
                "<h4>ILP-REST-Service-URL:</h4> <a href=\"" + serviceUrl + "\" target=\"_blank\"> " + serviceUrl+ " </a>" +
                "</body></html>";
    }

    @GetMapping("/uid")
    public String uid() {
        return "s1802871";
    }

    @GetMapping("actuator/health")
    public ResponseEntity<Map<String,String>> health(){
        return ResponseEntity.ok(Map.of("status","UP"));
    }

    @PostMapping("/distanceTo")
    public ResponseEntity<?> distanceTo(@RequestBody Map<String,Object> body){

        Map<String,Object> position1 = (Map<String,Object>)body.get("position1");
        Map<String,Object> position2 = (Map<String,Object>)body.get("position2");
        if (position1 == null || position2 == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status",400,"error","bad request","message","missing position1 or position2"));



        try {
            Double position1_lng = (Double) position1.get("lng"); // getting lng and lat
            Double position1_lat = (Double) position1.get("lat");
            Double position2_lng = (Double) position2.get("lng");
            Double position2_lat = (Double) position2.get("lat");

            if (position1_lng == null || position1_lat == null || position2_lng == null || position2_lat == null )
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status",400,"error","bad request","message","lack of lng or lat"));


            Double euclidianDistance = sqrt(pow(position2_lng - position1_lng, 2) + pow(position2_lat - position1_lat, 2)); // calculating disitance.

            return ResponseEntity.ok(euclidianDistance);
        }catch (ClassCastException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status",400,"error","bad request","message","lng or lat type error "));
        }




    }
    @PostMapping("/isCloseTo")
    public ResponseEntity<?> isCloseTo(@RequestBody Map<String,Object> body){

        ResponseEntity<?> distanceOfTwoPosition = distanceTo(body);;

        if (distanceOfTwoPosition.getStatusCode()!= HttpStatus.OK){
            return distanceOfTwoPosition;
        } else {
            return ResponseEntity.ok((double) distanceOfTwoPosition.getBody() < 0.00015);
        }
    }

    @PostMapping("/nextPosition")
    public ResponseEntity<?> nextPosition(@RequestBody Map<String,Object> body){
        Map<String, Object> lngAndlat = (Map<String, Object>) body.get("start");


        if (lngAndlat == null || lngAndlat.get("lng") == null || lngAndlat.get("lat")  == null ||  body.get("angle")  == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status",400,"error","bad request","message","missing lngOfPosition or latOfPosition or angleOfMovement"));

        BigDecimal lngOfPosition = new BigDecimal(lngAndlat.get("lng").toString());
        BigDecimal latOfPosition = new BigDecimal(lngAndlat.get("lat").toString());
        BigDecimal angleOfMovement = new BigDecimal((body.get("angle")).toString());
        BigDecimal step = new BigDecimal(0.00015);

        double angle = angleOfMovement.doubleValue();



        try {
            BigDecimal xCoefficient =  new BigDecimal(Math.cos(Math.toRadians(angle)));
            BigDecimal yCoefficient =  new BigDecimal(Math.sin(Math.toRadians(angle)));
            BigDecimal dx = step.multiply(xCoefficient);
            BigDecimal dy = step.multiply(yCoefficient);


            BigDecimal updatedLng = lngOfPosition.add(dx).setScale(6, RoundingMode.HALF_UP);
            BigDecimal updatedLat = latOfPosition.add(dy).setScale(6, RoundingMode.HALF_UP);

            return ResponseEntity.ok(Map.of("lat", updatedLat,"lng",updatedLng));
        }catch (ClassCastException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status",400,"error","bad request","message","lng or lat type error "));
        }
    }

    @PostMapping("/isInRegion")
    public ResponseEntity<?> isInRegion(@RequestBody Map<String,Object> body){
        Map<String,Object> position = (Map<String,Object>)body.get("position");
        if (position == null||position.get("lng") == null ||position.get("lat") == null )
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status",400,"error","bad request","message","missing  position or lng or lat"));
        Double position_lng = (Double) position.get("lng");
        Double position_lat = (Double) position.get("lat");
        Map<String,Object> region_test = (Map<String,Object>)body.get("region");
        if (region_test == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status",400,"error","bad request","message","missing  region"));


        Map<String,Object> region = (Map<String,Object>)body.get("region");
        List<Map<String,Object>> vertices =(List<Map<String,Object>>) region.get("vertices");

        if ((vertices== null)||(vertices.size() != 5)) // to determine wether the  5 vertices form a rectangle
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status",400,"error","bad request","message","invalid input, missing vertices or ordinates number less that 5 "));
        //for robustness input, when lng or lat in each vertice was missing, error will be catched and 400 will be cast.
        for (int i = 0; i < vertices.size(); i++) {
            Map<String, Object> point = vertices.get(i);
            Object lng = point.get("lng");
            Object lat = point.get("lat");

            if (lng == null || lat == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("status", 400, "error", "Missing lng or lat in vertex at index " + i));
            }
        }
//The user input data may be out of order. Here, we loop through the
//coordinate system to find the lower left and upper right corners of
//the rectangle. We then use the diagonal of the rectangle to
//determine the data.
        try {
            Map<String,Object> ordinatesFrist = vertices.get(0);
            Map<String,Object> ordinatesLast = vertices.get(4);

            double ordinatesFrist_lat = (Double) ordinatesFrist.get("lat");
            double ordinatesFrist_lng = (Double) ordinatesFrist.get("lng");
            double ordinatesLast_lat =  (Double) ordinatesLast.get("lat");
            double ordinatesLast_lng =  (Double) ordinatesLast.get("lng");


            if((ordinatesFrist_lat != ordinatesLast_lat)||(ordinatesFrist_lng != ordinatesLast_lng))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status",400,"error","bad request","message","invalid input,last ordinates does not match with the first one"));
        }catch (ClassCastException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status",400,"error","bad request","message","invalid input  "));
        }

        double x_max = Double.NEGATIVE_INFINITY;
        double x_min = Double.POSITIVE_INFINITY;
        double y_max = Double.NEGATIVE_INFINITY;
        double y_min = Double.POSITIVE_INFINITY;
        for(Map<String,Object> ordinates: vertices){
            if(x_max < (Double) ordinates.get("lat"))
                x_max = (Double) ordinates.get("lat");
            if(x_min > (Double) ordinates.get("lat"))
                x_min = (Double) ordinates.get("lat");
            if(y_max < (Double) ordinates.get("lng"))
                y_max = (Double) ordinates.get("lng");
            if(y_min > (Double) ordinates.get("lng"))
                y_min = (Double) ordinates.get("lng");
        }

        if ((y_min<=position_lng && position_lng <= y_max)&&(x_min <= position_lat && position_lat <= x_max))
            return ResponseEntity.ok(Map.of("isInRegion", true));
        else return ResponseEntity.ok(Map.of("isInRegion", false));



    }






}
