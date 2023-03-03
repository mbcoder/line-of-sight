/**
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.mycompany.app;
/*
 * Copyright 2023 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.mapping.RasterElevationSource;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.raster.Raster;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.MapView;

public class App extends Application {

    private MapView mapView;
    private Surface surface;

    @Override
    public void start(Stage stage) {

        try {
            // create stack pane and application scene
            BorderPane stackPane = new BorderPane();
            Scene scene = new Scene(stackPane);

            // set title, size, and add scene to stage
            stage.setTitle("Display Map Sample");
            stage.setWidth(800);
            stage.setHeight(700);
            stage.setScene(scene);
            stage.show();

            // authentication with an API key or named user is required to access basemaps and other location services
            String yourAPIKey = System.getProperty("apiKey");
            ArcGISRuntimeEnvironment.setApiKey(yourAPIKey);

            // create a map with the standard imagery basemap style
            ArcGISMap map = new ArcGISMap(BasemapStyle.OSM_STANDARD);

            // create a map view and set the map to it
            mapView = new MapView();
            mapView.setMap(map);

            // add a surface for elevation queries
            surface = new Surface();

            String rasterFile = "/Users/mark8487/ArcGISRuntime/local_server/Arran_10m_raster.tif";

            ArrayList<String> rasterList = new ArrayList<>();

            rasterList.add(rasterFile);

            RasterElevationSource rasterElevationSource = new RasterElevationSource(rasterList);
            rasterElevationSource.loadAsync();
            rasterElevationSource.addDoneLoadingListener(()-> {
                System.out.println("Elevation src loaded");
                surface.getElevationSources().add(rasterElevationSource);
            });
            surface.loadAsync();

            // display the surface as a raster layer for visual reference
            Raster rasterforDisplay = new Raster(rasterFile);
            RasterLayer rasterLayer = new RasterLayer(rasterforDisplay);
            map.getOperationalLayers().add(rasterLayer);

            // graphics overlay for showing stuff
            var graphicsOverlay = new GraphicsOverlay();
            mapView.getGraphicsOverlays().add(graphicsOverlay);

            // LOS points
            Point start = new Point(-589222.804907, 7468377.448381, 400.000000, SpatialReferences.getWebMercator());
            Point end = new Point(-574602.615448, 7473807.804465, 400.000000, SpatialReferences.getWebMercator());

            var vpFuture = mapView.setViewpointCenterAsync(start, 250000);
            vpFuture.addDoneListener(()-> {

                ArrayList<Point> losPoints = new ArrayList<>();
                losPoints.add(start);
                losPoints.add(end);

                // polyline
                PointCollection pointCollection = new PointCollection(losPoints);
                Polyline losLine = new Polyline(pointCollection);

                System.out.println("length " + GeometryEngine.length(losLine));
                System.out.println("pixel size " + pixelSize());

                System.out.println("elevation angle " + Math.toDegrees(elevationAngle(start, end)));
            });


            mapView.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.isStillSincePress()) {
                    // create a point from location clicked
                    Point2D mapViewPoint = new Point2D(e.getX(), e.getY());

                    Point geoPoint = mapView.screenToLocation(mapViewPoint);

                    graphicsOverlay.getGraphics().clear();

                    lineOfSight(start, geoPoint,pixelSize(), graphicsOverlay);


                    System.out.println(geoPoint);
                }
            });

            Button button = new Button("Press me");
            button.setOnAction(event -> {
                System.out.println("button pressed");
                System.out.println("elevation start " + elevationAtPoint(start));
                System.out.println("elevation end " + elevationAtPoint(end));

                long startTime = System.currentTimeMillis();
                System.out.println("LOS result " + lineOfSight(start, end, pixelSize(), graphicsOverlay));
                long endTime = System.currentTimeMillis();
                System.out.println("time taken " + (endTime - startTime));
            });

            stackPane.setTop(button);

            // add the map view to the stack pane
            stackPane.setCenter(mapView);
            //stackPane.getChildren().addAll(mapView);
        } catch (Exception e) {
            // on any error, display the stack trace.
            e.printStackTrace();
        }
    }

    private boolean lineOfSight(Point start, Point end, double sampleDistance, GraphicsOverlay graphicsOverlay) {

        // LOS result assuming positive
        boolean losResult = true;

        // angle between start and end points
        double observerAngle = elevationAngle(start, end);
        System.out.println("observer angle " + Math.toDegrees(observerAngle));

        // make a polyline for LOS
        ArrayList<Point> losPoints = new ArrayList<>();
        losPoints.add(start);
        losPoints.add(end);

        // polyline
        PointCollection pointCollection = new PointCollection(losPoints);
        Polyline losLine = new Polyline(pointCollection);

        // markers for graphics overlay results
        SimpleMarkerSymbol greenSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.GREEN,1);
        SimpleMarkerSymbol redSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED,1);

        // work out the sample points along the line of sight
        double distanceFromStart = 0;
        double length2d = GeometryEngine.distanceBetween(start, end);

        // estimate sample points
        System.out.println("sample points = "+ (length2d / sampleDistance));

        while (distanceFromStart < length2d) {
            Point samplePoint = GeometryEngine.createPointAlong(losLine, distanceFromStart);
            // add Z value to sample point
            Point samplePoint3D = new Point(samplePoint.getX(), samplePoint.getY(), elevationAtPoint(samplePoint), SpatialReferences.getWebMercator());
            //System.out.println("sample 3d point " + samplePoint3D);


            // get sample observer angle
            double sampleAngle = elevationAngle(start, samplePoint3D);
            //System.out.println("sample angle" + Math.toDegrees(sampleAngle));

            // if sample angle above observer angle los false
            if (sampleAngle > observerAngle) {
                losResult = false;
                //System.out.println("LOS not possible!");
            }

            // draw sample point
            Graphic graphic;
            if (losResult) {
                graphic = new Graphic(samplePoint, greenSymbol);
            } else {
                graphic = new Graphic(samplePoint, redSymbol);
            }
            graphicsOverlay.getGraphics().add(graphic);
            distanceFromStart += pixelSize();
        }


        return losResult;
    }

    private double pixelSize() {
        // app points
        Point2D pt1 = new Point2D(10,10);
        Point2D pt2 = new Point2D(10,11);

        // geo points
        Point geoPt1 = mapView.screenToLocation(pt1);
        Point geoPt2 = mapView.screenToLocation(pt2);

        // effective pixel size
        return  GeometryEngine.distanceBetween(geoPt1, geoPt2);
    }

    private double elevationAngle (Point start, Point end) {
        // diff in height
        double dh = end.getZ() - start.getZ();

        // length
        double length = GeometryEngine.distanceBetween(start, end);

        // angle (radians)
        double angle = Math.atan(dh / length);
        return angle;
    }

    private double elevationAtPoint (Point location) {

        double elevation = 0;
        var elevFuture = surface.getElevationAsync(location);

        try {
            elevation = elevFuture.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        return elevation;
    }

    /**
     * Stops and releases all resources used in application.
     */
    @Override
    public void stop() {

        if (mapView != null) {
            mapView.dispose();
        }
    }

    /**
     * Opens and runs application.
     *
     * @param args arguments passed to this application
     */
    public static void main(String[] args) {

        Application.launch(args);
    }

}
