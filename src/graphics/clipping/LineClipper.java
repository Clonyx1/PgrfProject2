package graphics.clipping;

import model.Line;
import model.Polygon;

public interface LineClipper {
    Line clip(Line line, Polygon boundary);
}
