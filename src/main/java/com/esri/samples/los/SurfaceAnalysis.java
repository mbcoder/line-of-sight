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
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javafx.scene.paint.Color;

/**
 * A class containing static methods for performing 3D surface analysis operations.
 */
public class SurfaceAnalysis {

  /**
   * A method for performing a line of sight calculation using a surface
   *
   * @param start starting point for the line of sight calculation
   * @param end endind point for the  line of sight calculation
   * @param sampleDistance distance in metres between sample points along line
   * @param graphicsOverlay the graphics overlay used to display the line of sight result
   * @param surface the surface used for performing the 3D line of sight operation
   * @return a boolean stating if there is a clear line of sight between the 2 points
   */
  public static boolean lineOfSight(
      Point start,
      Point end,
      double sampleDistance,
      GraphicsOverlay graphicsOverlay,
      Surface surface) {

    // LOS result assuming positive
    boolean losResult = true;

    // point at split between visible and not visible
    Point splitPoint = null;

    // angle between start and end points
    double observerAngle = elevationAngle(start, end);

    // make a polyline for LOS
    ArrayList<Point> losPoints = new ArrayList<>();
    losPoints.add(start);
    losPoints.add(end);

    // polyline
    PointCollection pointCollection = new PointCollection(losPoints);
    Polyline losLine = new Polyline(pointCollection);

    // symbols for graphics overlay results
    SimpleLineSymbol redLine = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID,Color.RED,2);
    SimpleLineSymbol greenLine = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID,Color.GREEN,2);

    // work out the sample points along the line of sight
    double distanceFromStart = 0;
    double length2d = GeometryEngine.distanceBetween(start, end);

    while (distanceFromStart < length2d) {
      Point samplePoint = GeometryEngine.createPointAlong(losLine, distanceFromStart);
      // add Z value to sample point
      Point samplePoint3D = new Point(samplePoint.getX(), samplePoint.getY(), elevationAtPoint(samplePoint, surface), SpatialReferences.getWebMercator());

      // get sample observer angle
      double sampleAngle = elevationAngle(start, samplePoint3D);

      // if sample angle above observer angle los false, record the split point
      if (sampleAngle > observerAngle) {
        losResult = false;
        splitPoint = samplePoint;
        break;
      }
      distanceFromStart += sampleDistance;
    }

    // green line of los = true
    if (losResult) {
      // draw green line
      Graphic losGraphic = new Graphic(losLine, greenLine);
      graphicsOverlay.getGraphics().add(losGraphic);
    } else {
      //draw visible part of line
      ArrayList<Point> visiblePoints = new ArrayList<>();
      visiblePoints.add(start);
      visiblePoints.add(splitPoint);
      PointCollection visiblePointCollection = new PointCollection(visiblePoints);
      Polyline visibleLine = new Polyline(visiblePointCollection);
      Graphic visibleGraphic = new Graphic(visibleLine, greenLine);
      graphicsOverlay.getGraphics().add(visibleGraphic);

      //draw not visible part of line
      ArrayList<Point> invisiblePoints = new ArrayList<>();
      invisiblePoints.add(splitPoint);
      invisiblePoints.add(end);
      PointCollection invisiblePointCollection = new PointCollection(invisiblePoints);
      Polyline invisibleLine = new Polyline(invisiblePointCollection);
      Graphic invisibleGraphic = new Graphic(invisibleLine, redLine);
      graphicsOverlay.getGraphics().add(invisibleGraphic);
    }
    return losResult;
  }

  /**
   * A method to calculate the angle above or below the horizontal between 2 points
   *
   * @param start start point with Z value set
   * @param end end point with Z value set
   * @return the angle in radians
   */
  private static double elevationAngle (Point start, Point end) {
    // diff in height
    double dh = end.getZ() - start.getZ();

    // length
    double length = GeometryEngine.distanceBetween(start, end);

    // angle (radians)
    double angle = Math.atan(dh / length);
    return angle;
  }


  /**
   * A method which returns the elevation at a given point on a surface.
   *
   * @param location a point which doesn't need the Z value set
   * @param surface the surface used to work out the elevation
   * @return the height above the globe surface of the elevation in Metres
   */
  public static double elevationAtPoint (Point location, Surface surface) {

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
}
