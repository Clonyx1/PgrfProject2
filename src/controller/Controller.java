package controller;

import graphics.filler.ScanlineFiller;
import graphics.filler.DequeSeedFiller;
import graphics.filler.SeedFiller;
import graphics.clipping.PolygonClipper;
import graphics.clipping.SutherlandHodgmanClipper;
import graphics.rasterizer.PolygonOutlineRasterizer;
import graphics.rasterizer.PolygonRasterizer;
import graphics.rasterizer.XiaolinWuLineRasterizer;
import model.Line;
import model.Point;
import model.Polygon;
import tools.LineTool;
import view.Canvas;
import view.gui.Toolbar;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;

import javax.swing.AbstractAction;

import static java.awt.event.KeyEvent.VK_C;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import static javax.swing.KeyStroke.getKeyStroke;

public class Controller {

    private final Canvas canvas;
    private final Toolbar toolbar;

    private Line previewLine;
    private LineTool lineTool;
    private final Polygon polygon;
    private final PolygonClipper clipper;
    private final Polygon clippingPolygon;
    private Polygon croppingRect;
    private final PolygonRasterizer polygonRasterizer;

    private Point seedFillPoint;
    private final SeedFiller seedFiller;
    private final ScanlineFiller scanlineFiller;

    private boolean seedFillMode;
    private boolean straightLineMode;
    private boolean cropMode;

    private static final int LINE_COLOR = Color.WHITE.getRGB();
    private static final int PREVIEW_COLOR = Color.RED.getRGB();
    private static final int ORIGINAL_COLOR = new Color(0x9099A3).getRGB();
    private static final int CLIPPED_COLOR = new Color(0xA4F2DF).getRGB();
    private static final int CLIPPING_POLYGON_COLOR = new Color(0xE6B85C).getRGB();
    private static final int FILL_COLOR = new Color(0x58BFA9).getRGB();

    private static final List<Point> CLIPPING_POLYGON_VERTICES = List.of(
            new Point(50, 50),
            new Point(200, 30),
            new Point(270, 150),
            new Point(180, 260),
            new Point(60, 200));

    public Controller(Canvas canvas, Toolbar toolbar) {
        this.canvas = canvas;
        this.toolbar = toolbar;
        lineTool = new LineTool();
        this.polygon = new Polygon();
        this.clipper = new SutherlandHodgmanClipper();
        this.scanlineFiller = new ScanlineFiller(canvas.getRaster());
        this.seedFiller = new DequeSeedFiller(canvas.getRaster());
        this.polygonRasterizer = new PolygonOutlineRasterizer(new XiaolinWuLineRasterizer(canvas.getRaster()));
        this.clippingPolygon = createPolygon(CLIPPING_POLYGON_VERTICES, CLIPPING_POLYGON_COLOR);
    }

    public void init() {
        toolbar.addClearListener(_ -> clearDrawing());
        toolbar.addCropListener(_ -> toggleCropMode());
        toolbar.addSeedFillListener(_ -> toggleSeedFillMode());
        canvas.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(getKeyStroke(VK_C, 0), "clearCanvas");
        canvas.setFocusable(true);
        canvas.requestFocusInWindow();
        canvas.getActionMap().put("clearCanvas", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                clearDrawing();
            }
        });

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) {
                    if (seedFillMode) {
                        fillAt(event);
                    } else if (cropMode) {
                        startRect(event);
                    } else {
                        startLine(event);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (event.getButton() != MouseEvent.BUTTON1) {
                    return;
                }

                if (previewLine != null) {
                    Line newPolygonLine = new Line(previewLine.getPoint1(), getPoint(event), LINE_COLOR);
                    if (straightLineMode) {
                        newPolygonLine = lineTool.straightenLine(newPolygonLine);
                    }
                    polygon.addLine(newPolygonLine);
                    previewLine = null;
                } else if (croppingRect != null) {
                    updateCroppingRect(getPoint(event), CLIPPING_POLYGON_COLOR);
                }

                render();
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                if (previewLine != null) {
                    previewLine = new Line(previewLine.getPoint1(), getPoint(event), PREVIEW_COLOR);
                    if (straightLineMode) {
                        previewLine = lineTool.straightenLine(previewLine);
                    }

                    render();
                } else if (croppingRect != null) {
                    updateCroppingRect(getPoint(event), PREVIEW_COLOR);
                    render();
                }
            }
        });

        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_SHIFT) {
                    straightLineMode = !straightLineMode;
                }
            }
        });

        render();
    }

    private void render() {
        canvas.clear();
        Polygon subject = withPreviewLine(polygon, previewLine);
        Polygon clipped;
        try {
            clipped = clipper.clip(subject, clippingPolygon);
        } catch (UnsupportedOperationException unfinishedClipper) {
            clipped = null;
        }
        if (clipped != null) {
            scanlineFiller.fill(clipped, FILL_COLOR);
        }
        polygonRasterizer.rasterize(withColor(polygon, ORIGINAL_COLOR), previewLine);
        if (clipped != null) {
            polygonRasterizer.rasterize(withColor(clipped, CLIPPED_COLOR), null);
        }
        polygonRasterizer.rasterize(clippingPolygon, null);
        if (cropMode && croppingRect != null) {
            polygonRasterizer.rasterize(croppingRect, null);
        }
        if (seedFillPoint != null) {
            seedFiller.fill(seedFillPoint.getX(), seedFillPoint.getY(), canvas.getBackgroundColor(), FILL_COLOR);
        }
        
        canvas.repaint();
    }

    private void clearDrawing() {
        polygon.clear();
        previewLine = null;
        seedFillPoint = null;
        seedFillMode = false;
        cropMode = false;
        croppingRect = null;
        toolbar.setSeedFillMode(false);
        render();
    }

    private void toggleCropMode() {
        cropMode = !cropMode;
        toolbar.setCropMode(cropMode);
        render();
    }

    private void toggleSeedFillMode() {
        seedFillMode = !seedFillMode;
        toolbar.setSeedFillMode(seedFillMode);
        render();
    }

    private void fillAt(MouseEvent event) {
        Point seed = getPoint(event);
        Integer seedColor = canvas.getRaster().getPixel(seed.getX(), seed.getY());
        if (seedFillPoint != null && seedColor != null && seedColor == FILL_COLOR) {
            seedFillPoint = null;
            render();
            return;
        }

        boolean filled = seedFiller.fill(seed.getX(), seed.getY(), canvas.getBackgroundColor(), FILL_COLOR);
        if (filled) {
            seedFillPoint = seed;
            render();
        }
    }

    private Point getPoint(MouseEvent event) {
        return new Point(event.getX(), event.getY());
    }

    private void startLine(MouseEvent event) {
        seedFillPoint = null;
        Point startPoint = polygon.getLastPoint();
        if (startPoint == null) {
            startPoint = getPoint(event);
        }
        previewLine = new Line(startPoint, startPoint, PREVIEW_COLOR);
        render();
    }

    private void startRect(MouseEvent event) {
        seedFillPoint = null;
        previewLine = null;
        updateCroppingRect(getPoint(event), PREVIEW_COLOR);
        render();
    }

    private void updateCroppingRect(Point bottomRight, int color) {
        Point topLeft = croppingRect == null
                ? bottomRight
                : croppingRect.getLines().get(0).getPoint1();
        
        Point topRight = new Point(bottomRight.getX(), topLeft.getY());
        Point bottomLeft = new Point(topLeft.getX(), bottomRight.getY());

        croppingRect = new Polygon();
        croppingRect.addLine(new Line(topLeft, topRight, color));
        croppingRect.addLine(new Line(topRight, bottomRight, color));
        croppingRect.addLine(new Line(bottomRight, bottomLeft, color));
    }

    private Polygon withColor(Polygon source, int color) {
        Polygon result = new Polygon();
        for (Line line : source.getLines()) {
            result.addLine(new Line(line.getPoint1(), line.getPoint2(), color));
        }
        return result;
    }

    private Polygon withPreviewLine(Polygon source, Line preview) {
        Polygon result = new Polygon();
        for (Line line : source.getLines()) {
            result.addLine(line);
        }
        if (preview != null) {
            result.addLine(preview);
        }
        return result;
    }

    private Polygon createPolygon(List<? extends Point> vertices, int color) {
        Polygon result = new Polygon();
        for (int i = 0; i + 1 < vertices.size(); i++) {
            result.addLine(new Line(vertices.get(i), vertices.get(i + 1), color));
        }
        return result;
    }

}
