package sc.lazymath.ocr.math.formulatree;

import java.util.List;

import sc.lazymath.ocr.imageprocessing.RasterRegion;

/**
 * Created by nikola42 on 12/29/2014.
 */
public abstract class AbstractNode {
    protected RasterRegion region;

    public abstract String getCharacters();

    public abstract List<RasterRegion> getRasterRegions();

    public RasterRegion getRasterRegion() {
        return region;
    }

    public void setRasterRegion(RasterRegion region) {
        this.region = region;
    }
}
