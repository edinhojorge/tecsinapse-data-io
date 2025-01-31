/*
 * Tecsinapse Data Input and Output
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later
 * See the LICENSE file in the root directory or <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package br.com.tecsinapse.dataio.importer.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.Setter;

import br.com.tecsinapse.dataio.ExporterFormatter;
import br.com.tecsinapse.dataio.annotation.TableCellMapping;
import br.com.tecsinapse.dataio.converter.Converter;
import br.com.tecsinapse.dataio.converter.group.Default;
import br.com.tecsinapse.dataio.importer.Importer;
import br.com.tecsinapse.dataio.importer.ImporterUtils;
import br.com.tecsinapse.dataio.importer.Parser;
import br.com.tecsinapse.dataio.type.FileType;
import br.com.tecsinapse.dataio.util.CsvUtil;

public class CsvParser<T> implements Parser<T> {

    private final Class<T> clazz;
    @Setter
    private Class<?> group;
    private List<String> csvLines;
    @Setter
    private int headersRows = Importer.DEFAULT_START_ROW;
    @Getter @Setter
    private ExporterFormatter exporterFormatter = ExporterFormatter.ENGLISH;
    @Getter @Setter
    private boolean ignoreBlankLinesAtEnd = false;

    public CsvParser(Class<T> clazz, File file, Charset charset, int afterLine, Class<?> group) throws IOException {
        this(clazz, file, charset, group);
        this.headersRows = afterLine;
    }

    public CsvParser(Class<T> clazz, InputStream input, Charset charset, int afterLine, Class<?> group) throws IOException {
        this(clazz, input, charset, group);
        this.headersRows = afterLine;
    }

    public CsvParser(Class<T> clazz, List<String> csvLines) {
        this(clazz, csvLines, Default.class);
    }

    public CsvParser(Class<T> clazz, File file, Charset charset) throws IOException {
        this(clazz, file, charset, Default.class);
    }

    public CsvParser(Class<T> clazz, File file, Charset charset, Class<?> group) throws IOException {
        this(clazz, CsvUtil.processCSV(new FileInputStream(file), charset), group);
    }

    public CsvParser(Class<T> clazz, InputStream inputStream, Charset charset) throws IOException {
        this(clazz, inputStream, charset, Default.class);
    }

    public CsvParser(Class<T> clazz, InputStream inputStream, Charset charset, Class<?> group) throws IOException {
        this(clazz, CsvUtil.processCSV(inputStream, charset), group);
    }

    public CsvParser(Class<T> clazz, List<String> csvLines, Class<?> group) {
        this.clazz = clazz;
        this.csvLines = csvLines;
        this.group = group;
    }

    @Override
    public int getNumberOfSheets() {
        return 1;
    }

    @Override
    public void setSheetNumber(int sheetNumber) {
        // nothing to do when is CSV
    }

    @Override
    public void setLastsheet(boolean lastsheet) {
        // nothing to do when is CSV
    }

    @Override
    public void setFirstVisibleSheet() {
        // nothing to do when is CSV
    }

    @Override
    public int getSheetNumber() {
        return 0;
    }

    @Override
    public void setSheetNumberAsFirstNotHidden() {
        // nothing to do when is CSV
    }

    @Override
    public FileType getFileType() {
        return FileType.CSV;
    }

    @Override
    public List<List<String>> getLines() {
        return Collections.emptyList();
    }

    /**
     * Parser file to list of T objects
     *
     * Obs.: No read de first line.
     *
     * @return List of T object
     * @throws IllegalAccessException IllegalAccessException
     * @throws InstantiationException InstantiationException
     * @throws InvocationTargetException InvocationTargetException
     * @throws NoSuchMethodException NoSuchMethodException
     */
    @Override
    public List<T> parse() throws IllegalAccessException, InstantiationException,
            InvocationTargetException, NoSuchMethodException {
        List<T> list = new ArrayList<>();

        Map<Method, TableCellMapping> cellMappingByMethod = ImporterUtils.getMappedMethods(clazz, group);

        final Constructor<T> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        for (int i = 0; i < csvLines.size(); i++) {
            final String line = csvLines.get(i);
            if ((i + 1) <= headersRows) {
                continue;
            }

            List<String> fields = split(line);
            T instance = constructor.newInstance();

            for (Entry<Method, TableCellMapping> methodTcm : cellMappingByMethod.entrySet()) {
                Method method = methodTcm.getKey();
                method.setAccessible(true);

                TableCellMapping tcm = methodTcm.getValue();
                String value = getValueOrEmpty(fields, tcm.columnIndex());
                Converter<?, ?> converter = tcm.converter().newInstance();
                Object obj = converter.apply(value);
                method.invoke(instance, obj);
            }
            list.add(instance);
        }
        return list;
    }

    private String getValueOrEmpty(List<String> fields, int index) {
        if (fields.isEmpty() || fields.size() <= index) {
            return "";
        }
        return fields.get(index);
    }

    private List<String> split(String line) {
        int index;
        int lastIndex = 0;

        List<String> linhaParseadaPorAspas = new ArrayList<>();

        /*
         * Percorre a linha em busca de ;
         * depois verifica se entre 2 ; existem aspas
         * Se houver, é preciso ignorar os ; internos às aspas
         */
        while (lastIndex != -1 && lastIndex < line.length()) {
            index = line.indexOf(';', lastIndex);

            if (index == -1) {
                //ultima coluna
                linhaParseadaPorAspas.add(line.substring(lastIndex).replace(";", ""));
                break;
            } else {
                String coluna = line.substring(lastIndex, index + 1);

                if (temAspas(coluna)) {
                    index = getFinalColuna(line.substring(lastIndex), lastIndex);
                    if (index == -1) {
                        //ultima coluna
                        linhaParseadaPorAspas.add(line.substring(lastIndex).replace("\"\"", "\"").trim());
                        break;
                    }
                    coluna = substringNormalizada(line, lastIndex, index - 1);
                    linhaParseadaPorAspas.add(coluna);
                    lastIndex = index;
                } else {
                    linhaParseadaPorAspas.add(coluna.replace(";", ""));
                    lastIndex = index == -1 ? -1 : index + 1;
                }
            }
        }

        return linhaParseadaPorAspas;
    }

    private int getFinalColuna(String substring, int inicio) {
        char[] chars = substring.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\"') {
                for (int j = i + 1; j < chars.length; j++) {
                    if (chars[j] == '\"') {
                        return getFinalColuna(substring.substring(j + 1), inicio + j + 1);
                    }
                }
            }

            if (chars[i] == ';') {
                return i + inicio + 1;
            }
        }

        return -1;
    }

    private boolean temAspas(String column) {
        return column.indexOf('"') != -1;
    }

    private String substringNormalizada(String line, int i, int f) {
        line = line.substring(i, f - 1).trim();
        if (line.startsWith("\"")) {
            line = line.substring(1);
        }
        if (line.endsWith("\"")) {
            line = line.substring(0, line.length() - 1);
        }

        return line.replace("\"\"", "\"").trim();
    }

    @Override
    public void close() {
        //nada parser é feito atualmente no construtor
    }
}
