package sc.lazymath.ocr.math.root;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sc.lazymath.ocr.imageprocessing.RasterRegion;
import sc.lazymath.ocr.math.AbstractNode;
import sc.lazymath.ocr.math.simplenode.SimpleNodeUtil;
import sc.lazymath.ocr.math.simplenode.SimpleNode;

public class RootUtil {
	/**
	 * Creates root nodes based on passed raster regions and already created
	 * nodes. Check if raster regions contain other raster regions or nodes.
	 *
	 * @param regions
	 *            raster regions for proccessing
	 * @param nodes
	 *            list of nodes already created nodes
	 * @return list of created root nodes
	 */
	public static List<AbstractNode> getNthRootNodes(List<RasterRegion> regions,
			List<AbstractNode> nodes) {
		List<AbstractNode> ret = new ArrayList<>();

		// sort regions by size
		List<RasterRegion> sortedBySize = new ArrayList<>(regions);
		Collections.sort(sortedBySize, new Comparator<RasterRegion>() {
			@Override
			public int compare(RasterRegion rr1, RasterRegion rr2) {
				return (int) (rr2.majorAxisLength * rr2.minorAxisLength - rr1.majorAxisLength
						* rr1.minorAxisLength);
			}
		});

		// ignoreRegions list is used for ignoring regions that have been used,
		// that is are elements in another root node
		List<RasterRegion> ignoreRegions = new ArrayList<>();
		for (RasterRegion r1 : regions) {
			if (!ignoreRegions.contains(r1)) {
				// call method for checking if raster region is a root region
				NthRootNode nthRootNode = createNthRootNode(r1, regions, nodes);

				// nthRootNode will be null if region is not root node
				if (nthRootNode != null) {
					ret.add(nthRootNode);

					// add all elements in the root node to the ignoreRegions
					// list
					ignoreRegions.addAll(nthRootNode.getRasterRegions());
				}
			}
		}

		// remove all elements from ignoreRegions list from original regions
		// list
		regions.removeAll(ignoreRegions);

		return ret;
	}

	/**
	 * Creates a root node if rootRegion contains other raster regions or
	 * already created nodes.
	 *
	 * @param rootRegion
	 *            potential root region
	 * @param regions
	 *            list of potential root elements and root degree regions
	 * @param nodes
	 *            list of already created nodes
	 * @return created NthRootNode node or null
	 */
	public static NthRootNode createNthRootNode(RasterRegion rootRegion,
			List<RasterRegion> regions, List<AbstractNode> nodes) {
		NthRootNode ret = null;

		RasterRegion degree = null;
		List<RasterRegion> elements = new ArrayList<>();
		List<AbstractNode> nodeElements = new ArrayList<>();

		List<AbstractNode> nodesToRemove = new ArrayList<AbstractNode>();

		// check if already created nodes are inside the rootRegion
		for (AbstractNode node : nodes) {
			Point center = node.getCenter();

			if ((center.x > rootRegion.minX) && (center.x < rootRegion.maxX)
					&& (center.y > rootRegion.minY) && (center.y < rootRegion.maxY)) {
				// if the node is inside the region add it as root element
				nodeElements.add(node);
				nodesToRemove.add(node);
			}
		}

		// remove all nodes that have been added to the root
		nodes.removeAll(nodesToRemove);

		// check if regions are inside the rootRegion
		for (RasterRegion region : regions) {
			if (region != rootRegion) {
				boolean used = false;

				// check if region is root degree for rootRegion
				double cornerSize = (rootRegion.maxY - region.minY) / 2;
				if ((region.xM > rootRegion.minX) && (region.xM < (rootRegion.minX + cornerSize))
						&& (region.yM > rootRegion.minY) && (region.yM < rootRegion.maxY)) {
					degree = region;
					used = true;
				}

				// check if region is inside rootRegion
				if (!used) {
					if ((region.xM > rootRegion.minX) && (region.xM < rootRegion.maxX)
							&& (region.yM > rootRegion.minY) && (region.yM < rootRegion.maxY)) {
						elements.add(region);
					}
				}
			}
		}

		// if elements or elementNodes are not empty, rootRegions is a root node
		if (!elements.isEmpty() || !nodeElements.isEmpty()) {
			// create a root node
			ret = new NthRootNode();
			ret.setRasterRegion(rootRegion);

			ret.setDegree(new SimpleNode(degree));

			// add elements and check if there are root elements
			ret.addElements(getNthRootNodes(elements, nodeElements));
			ret.addElements(SimpleNodeUtil.getSimpleNodes(elements));

			ret.addElements(nodeElements);
		}

		return ret;
	}
}
