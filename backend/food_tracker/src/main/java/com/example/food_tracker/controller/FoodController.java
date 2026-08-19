package com.example.food_tracker.controller;

import com.example.food_tracker.model.FoodItem;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FoodController {

    // ---- config ----
    private static final double DAILY_LIMIT = 2000;

    // baseline per 100g: {calories, protein, carbs, fats}
    private static final Map<String, double[]> BASELINE = new HashMap<>();
    static {
        BASELINE.put("rice", new double[]{130, 2.7, 28, 0.3});
        BASELINE.put("chicken", new double[]{165, 31, 0, 3.6});
        BASELINE.put("egg", new double[]{155, 13, 1.1, 11});
        BASELINE.put("banana", new double[]{89, 1.1, 23, 0.3});
        BASELINE.put("bread", new double[]{265, 9, 49, 3.2});
        BASELINE.put("paneer", new double[]{265, 18, 1.2, 20.8});
        BASELINE.put("default", new double[]{200, 8, 20, 8});
    }

    // ---- in-memory state ----
    private final List<FoodItem> items = new ArrayList<>();
    private final AtomicLong idGen = new AtomicLong(1);

    // ---- helpers ----
    private double[] lookupBaseline(String name) {
        String key = name == null ? "" : name.trim().toLowerCase();
        for (String k : BASELINE.keySet()) {
            if (key.contains(k)) return BASELINE.get(k);
        }
        return BASELINE.get("default");
    }

    private Map<String, Object> buildResponse() {
        double totalCal = 0, totalProtein = 0, totalCarbs = 0, totalFats = 0;
        for (FoodItem f : items) {
            totalCal += f.calories;
            totalProtein += f.protein;
            totalCarbs += f.carbs;
            totalFats += f.fats;
        }
        Map<String, Object> totals = new HashMap<>();
        totals.put("calories", Math.round(totalCal * 10.0) / 10.0);
        totals.put("protein", Math.round(totalProtein * 10.0) / 10.0);
        totals.put("carbs", Math.round(totalCarbs * 10.0) / 10.0);
        totals.put("fats", Math.round(totalFats * 10.0) / 10.0);
        totals.put("limit", DAILY_LIMIT);

        Map<String, Object> resp = new HashMap<>();
        resp.put("items", items);
        resp.put("totals", totals);
        resp.put("exceeded", totalCal > DAILY_LIMIT);
        return resp;
    }

    // ---- endpoints ----

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        return buildResponse();
    }

    @PostMapping("/foods")
    public Map<String, Object> addFood(@RequestBody Map<String, Object> body) {
        String name = String.valueOf(body.get("name"));
        double grams = Double.parseDouble(String.valueOf(body.get("grams")));

        double[] base = lookupBaseline(name); // {cal, protein, carbs, fat} per 100g
        double scale = grams / 100.0;

        FoodItem item = new FoodItem(
                idGen.getAndIncrement(),
                name,
                grams,
                base[0] * scale,
                base[1] * scale,
                base[2] * scale,
                base[3] * scale
        );
        items.add(item);
        return buildResponse();
    }

    // simulates the "Image Upload" AI scanner with a fixed mock result
    @PostMapping("/foods/mock-upload")
    public Map<String, Object> mockUpload() {
        double[] base = BASELINE.get("chicken");
        double grams = 350;
        double scale = grams / 100.0;
        FoodItem item = new FoodItem(
                idGen.getAndIncrement(),
                "Grilled Chicken Bowl (scanned)",
                grams,
                base[0] * scale,
                base[1] * scale,
                base[2] * scale,
                base[3] * scale
        );
        items.add(item);
        return buildResponse();
    }

    @DeleteMapping("/foods/{id}")
    public Map<String, Object> deleteFood(@PathVariable long id) {
        items.removeIf(f -> f.id == id);
        return buildResponse();
    }
}