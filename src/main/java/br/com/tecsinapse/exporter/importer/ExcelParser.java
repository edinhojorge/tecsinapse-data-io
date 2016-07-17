/*
 * TecSinapse Exporter
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later
 * See the LICENSE file in the root directory or <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package br.com.tecsinapse.exporter.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import br.com.tecsinapse.exporter.ExcelType;
import br.com.tecsinapse.exporter.ImporterType;
import br.com.tecsinapse.exporter.converter.group.Default;
import br.com.tecsinapse.exporter.importer.parser.SpreadsheetParser;

/**
 * Usage new Class FileImporter
 */
@Deprecated
public class ExcelParser<T> extends SpreadsheetParser<T> {

    public ExcelParser(Class<T> clazz, File file) throws IOException {
        this(clazz, new FileInputStream(file), ExcelType.getExcelType(file.getName()));
    }

    public ExcelParser(Class<T> clazz, File file, int afterLine) throws IOException {
        this(clazz, new FileInputStream(file), ExcelType.getExcelType(file.getName()), afterLine);
    }

    public ExcelParser(Class<T> clazz, File file, int afterLine, boolean lastSheet, ImporterXLSXType importerXLSXType) throws IOException {
        this(clazz, file, afterLine, lastSheet, importerXLSXType, Default.class);
    }

    public ExcelParser(Class<T> clazz, File file, int afterLine, boolean lastSheet, ImporterXLSXType importerXLSXType, Class<?> group) throws IOException {
        this(clazz, new FileInputStream(file), ExcelType.getExcelType(file.getName()), afterLine, lastSheet, importerXLSXType, group);
    }

    public ExcelParser(Class<T> clazz, InputStream inputStream, ExcelType type) {
        this(clazz, inputStream, type, Default.class);
    }

    public ExcelParser(Class<T> clazz, InputStream inputStream, ExcelType type, Class<?> group) {
        this(clazz, inputStream, type, Importer.DEFAULT_START_ROW, group);
    }

    public ExcelParser(Class<T> clazz, InputStream inputStream, ExcelType type, int afterLine) {
        this(clazz, inputStream, type, afterLine, Default.class);
    }

    public ExcelParser(Class<T> clazz, InputStream inputStream, ExcelType type, int afterLine, Class<?> group) {
        this(clazz, inputStream, type, afterLine, false, ImporterXLSXType.DEFAULT, group);
    }

    public ExcelParser(Class<T> clazz, InputStream inputStream, ExcelType type, int afterLine, boolean isLastSheet, ImporterXLSXType importerXLSXType) {
        this(clazz, inputStream, type, afterLine, isLastSheet, importerXLSXType, Default.class);
    }

    public ExcelParser(Class<T> clazz, InputStream inputStream, ExcelType type, int afterLine, boolean isLastSheet, ImporterXLSXType importerXLSXType, Class<?> group) {
        this(clazz, inputStream, group, excelTypeToImpoterType(type));
        if (isLastSheet) {
            setSheetNumber(getNumberOfSheets() - 1);
        }
        setHeadersRows(afterLine);
    }

    private static ImporterType excelTypeToImpoterType(ExcelType type) {
        if (type == ExcelType.XLSX) {
            return ImporterType.XLSX;
        }
        if (type == ExcelType.XLSM) {
            return ImporterType.XLSM;
        }
        return ImporterType.XLS;
    }

    private ExcelParser(Class<T> clazz, InputStream inputStream, Class<?> group, ImporterType importerType) {
        super(clazz, inputStream, group, importerType);
    }


    public void setAfterLine(int afterLine) {
        setHeadersRows(afterLine);
    }

}
