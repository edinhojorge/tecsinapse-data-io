/*
 * Tecsinapse Data Input and Output
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later
 * See the LICENSE file in the root directory or <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */

package br.com.tecsinapse.dataio.style;

import static br.com.tecsinapse.dataio.util.WorkbookUtil.toIndexedColorMap;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class CellStyleBorder {

    private HSSFColor borderColor;
    private boolean left;
    private boolean right;
    private boolean top;
    private boolean bottom;
    private short size = 1;

    public CellStyleBorder(boolean left, boolean right, boolean top, boolean bottom) {
        this(Colors.BLACK, left, right, top, bottom);
    }

    public CellStyleBorder(HSSFColor borderColor, boolean left, boolean right, boolean top, boolean bottom) {
        this.borderColor = borderColor;
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }


    public CellStyle toCellStyle(CellStyle cellStyle) {
        if (cellStyle == null || !left && !right && !bottom && !top) {
            return cellStyle;
        }
        if (left) {
            cellStyle.setBorderLeft(BorderStyle.THIN);
            if (cellStyle instanceof XSSFCellStyle) {
                ((XSSFCellStyle)cellStyle).setLeftBorderColor(new XSSFColor(toIndexedColorMap(borderColor)));
            } else {
                cellStyle.setLeftBorderColor(borderColor.getIndex());
            }
        }
        if (right) {
            cellStyle.setBorderRight(BorderStyle.THIN);
            if (cellStyle instanceof XSSFCellStyle) {
                ((XSSFCellStyle)cellStyle).setRightBorderColor(new XSSFColor(toIndexedColorMap(borderColor)));
            } else {
                cellStyle.setRightBorderColor(borderColor.getIndex());
            }
        }
        if (bottom) {
            cellStyle.setBorderBottom(BorderStyle.THIN);
            if (cellStyle instanceof XSSFCellStyle) {
                ((XSSFCellStyle)cellStyle).setBottomBorderColor(new XSSFColor(toIndexedColorMap(borderColor)));
            } else {
                cellStyle.setBottomBorderColor(borderColor.getIndex());
            }
        }
        if (top) {
            cellStyle.setBorderTop(BorderStyle.THIN);
            if (cellStyle instanceof XSSFCellStyle) {
                ((XSSFCellStyle)cellStyle).setTopBorderColor(new XSSFColor(toIndexedColorMap(borderColor)));
            } else {
                cellStyle.setTopBorderColor(borderColor.getIndex());
            }
        }
        return cellStyle;
    }

    public String toCss() {
        String format = "border%s:solid %s %dpx;";
        if (left && right && bottom && top) {
            return String.format(format, "", StyleColorUtil.toHexColor(borderColor), size);
        }
        if (!left && !right && !bottom && !top) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        if (left) {
            builder.append(String.format(format, "-left", StyleColorUtil.toHexColor(borderColor), size));
        }
        if (right) {
            builder.append(String.format(format, "-right", StyleColorUtil.toHexColor(borderColor), size));
        }
        if (bottom) {
            builder.append(String.format(format, "-bottom", StyleColorUtil.toHexColor(borderColor), size));
        }
        if (top) {
            builder.append(String.format(format, "-top", StyleColorUtil.toHexColor(borderColor), size));
        }
        return builder.toString();
    }

    public CellStyleBorder duplicate() {
        return new CellStyleBorder(getBorderColor(), isLeft(), isRight(), isTop(), isBottom());
    }

}
