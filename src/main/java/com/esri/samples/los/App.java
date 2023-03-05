/**
 * Copyright 2023 Esri
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

package com.esri.samples.los;

import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.mapping.RasterElevationSource;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.raster.Raster;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
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

            // LOS start point 1 metre above surface
            Point start2D = new Point(-589222.804907, 7468377.448381, SpatialReferences.getWebMercator());
            Point start3D = new Point(start2D.getX(), start2D.getY(), SurfaceAnalysis.elevationAtPoint(start2D, surface), SpatialReferences.getWebMercator());

            mapView.setViewpointCenterAsync(start2D, 250000);

            mapView.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.isStillSincePress()) {
                    // create a point from location clicked
                    Point2D mapViewPoint = new Point2D(e.getX(), e.getY());

                    Point geoPoint2D = mapView.screenToLocation(mapViewPoint);

                    // 3d point slightly above surface (1 metre)
                    Point geoPoint3D = new Point(geoPoint2D.getX(), geoPoint2D.getY(), SurfaceAnalysis.elevationAtPoint(geoPoint2D, surface)+ 1, SpatialReferences.getWebMercator());

                    graphicsOverlay.getGraphics().clear();

                    long startTime = System.currentTimeMillis();
                    System.out.println("LOS result " + SurfaceAnalysis.lineOfSight(start3D, geoPoint3D, pixelSize(), graphicsOverlay, surface));
                    long endTime = System.currentTimeMillis();
                    System.out.println("time taken " + (endTime - startTime));
                }
            });

            // add the map view to the stack pane
            stackPane.setCenter(mapView);
            //stackPane.getChildren().addAll(mapView);
        } catch (Exception e) {
            // on any error, display the stack trace.
            e.printStackTrace();
        }
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
