/*
 * Tecsinapse Data Input and Output
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later
 * See the LICENSE file in the root directory or <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package br.com.tecsinapse.dataio;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.poi.ss.util.DateFormatConverter;

import br.com.tecsinapse.dataio.type.CellType;
import br.com.tecsinapse.dataio.util.Constants;
import br.com.tecsinapse.dataio.util.ExporterDateUtils;
import br.com.tecsinapse.dataio.util.ExporterDateUtils.DateType;

public class ExporterFormatter {

    public static final ExporterFormatter BRAZILIAN = new ExporterFormatter(Constants.LOCALE_PT_BR);
    public static final ExporterFormatter ENGLISH = new ExporterFormatter(Locale.ENGLISH);

    private static final String CURRENCY_SYMBOL_PATTERN = "¤";

    private final SimpleDateFormat dateTimeFormat;
    private final SimpleDateFormat dateFormat;
    private final SimpleDateFormat timeFormat;
    private final DecimalFormat decimalFormat;
    private final DecimalFormat integerFormat;
    private final DecimalFormat currencyFormat;
    private final String cellDateTimeFormat;
    private final String cellDateFormat;
    private final String cellTimeFormat;
    private final String cellCurrencyFormat;
    private final String cellDecimalFormat;
    private final String cellIntegerFormat;
    private final Locale locale;

    public ExporterFormatter(Locale locale) {
        this.locale = locale;
        this.dateTimeFormat = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
        this.dateFormat = (SimpleDateFormat) SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        this.timeFormat = (SimpleDateFormat) SimpleDateFormat.getTimeInstance(DateFormat.SHORT, locale);
        this.decimalFormat = (DecimalFormat) DecimalFormat.getInstance(locale);
        this.integerFormat = (DecimalFormat) DecimalFormat.getIntegerInstance(locale);
        this.currencyFormat = (DecimalFormat) DecimalFormat.getCurrencyInstance(locale == Locale.ENGLISH ? Locale.US : locale);
        this.cellDateTimeFormat = DateFormatConverter.convert(locale, dateTimeFormat.toPattern());
        this.cellDateFormat = DateFormatConverter.convert(locale, dateFormat.toPattern());
        this.cellTimeFormat = DateFormatConverter.convert(locale, timeFormat.toPattern());
        this.cellDecimalFormat = DateFormatConverter.convert(locale, decimalFormat.toPattern());
        this.cellIntegerFormat = DateFormatConverter.convert(locale, integerFormat.toPattern());
        this.cellCurrencyFormat = DateFormatConverter.convert(locale, getCurrencyFormatString());
    }

    public ExporterFormatter(String dateTimeFormat, String dateFormat, String timeFormat, String decimalFormat, String integerFormat, String currencyFormat) {
        this(dateTimeFormat, dateFormat, timeFormat, decimalFormat, integerFormat, currencyFormat, Locale.getDefault());
    }

    public ExporterFormatter(String dateTimeFormat, String dateFormat, String timeFormat, String decimalFormat, String integerFormat, String currencyFormat, Locale locale) {
        this.locale = locale;
        this.dateTimeFormat = new SimpleDateFormat(dateTimeFormat);
        this.dateFormat = new SimpleDateFormat(dateFormat, locale);
        this.timeFormat = new SimpleDateFormat(timeFormat, locale);
        this.decimalFormat = new DecimalFormat(decimalFormat, DecimalFormatSymbols.getInstance(locale));
        this.integerFormat = new DecimalFormat(integerFormat, DecimalFormatSymbols.getInstance(locale));
        this.currencyFormat = new DecimalFormat(currencyFormat, DecimalFormatSymbols.getInstance(locale));
        this.cellDateTimeFormat = DateFormatConverter.convert(locale, dateTimeFormat);
        this.cellDateFormat = DateFormatConverter.convert(locale, dateFormat);
        this.cellTimeFormat = DateFormatConverter.convert(locale, timeFormat);
        this.cellCurrencyFormat = DateFormatConverter.convert(locale, currencyFormat);
        this.cellDecimalFormat = DateFormatConverter.convert(locale, decimalFormat);
        this.cellIntegerFormat = DateFormatConverter.convert(locale, integerFormat);
    }

    private String getCurrencyFormatString() {
        String cf = this.currencyFormat.toPattern();
        cf = cf.replace(CURRENCY_SYMBOL_PATTERN, this.currencyFormat.getPositivePrefix());
        return cf;
    }

    public String getLocalDateTimeFormat() {
        return dateTimeFormat.toPattern();
    }

    public String getLocalDateFormat() {
        return dateFormat.toPattern();
    }

    public String getLocalTimeFormat() {
        return timeFormat.toPattern();
    }

    public String getDecimalFormater() {
        return decimalFormat.toPattern();
    }

    public String getIntegerFormat() {
        return integerFormat.toPattern();
    }

    public String getCurrencyFormat() {
        return currencyFormat.toPattern();
    }

    public Locale getLocale() {
        return this.locale;
    }

    public String formatDate(Date date) {
        return dateFormat.format(date);
    }

    public String formatTime(Date date) {
        return timeFormat.format(date);
    }

    public String formatDateTime(Date date) {
        return dateTimeFormat.format(date);
    }

    public String formatNumber(Number number) {
        if (number instanceof Integer || number instanceof Long) {
            return integerFormat.format(number);
        }
        return decimalFormat.format(number);
    }

    public String formatCurrency(Number number) {
        return currencyFormat.format(number);
    }

    public String formatByType(Object o, boolean isCurrency) {
        if (o instanceof Date) {
            return formatByDateType((Date) o);
        }
        if (o instanceof Number) {
            if (isCurrency) {
                return formatCurrency((Number) o);
            }
            return formatNumber((Number) o);
        }
        return o.toString();
    }

    public String formatByDateType(Date date) {
        DateType dateType = ExporterDateUtils.getDateType(date);
        if (dateType == DateType.NO_DATE) {
            return null;
        }
        if (dateType == DateType.DATE) {
            return dateFormat.format(date);
        }
        if (dateType == DateType.TIME) {
            return timeFormat.format(date);
        }
        return dateTimeFormat.format(date);
    }

    public String getCellStringFormatByType(Object o, boolean isCurrency) {
        if (o instanceof Date) {
            Date date = (Date) o;
            DateType dateType = ExporterDateUtils.getDateType(date);
            if (dateType == DateType.NO_DATE) {
                return null;
            }
            if (dateType == DateType.DATE) {
                return cellDateFormat;
            }
            if (dateType == DateType.TIME) {
                return cellTimeFormat;
            }
            return cellDateTimeFormat;
        }
        if (o instanceof Integer || o instanceof Long) {
            return cellIntegerFormat;
        }
        if (o instanceof Number && isCurrency) {
            return cellCurrencyFormat;
        }
        if (o instanceof Number) {
            return cellDecimalFormat;
        }
        return null;
    }

    public String getCellStringFormatByUserType(Object o, CellType cellType) {
        if (cellType == CellType.DATE_TYPE) {
            return cellDateFormat;
        }
        if (cellType == CellType.TIME_TYPE) {
            return cellTimeFormat;
        }
        if (cellType == CellType.DATETIME_TYPE) {
            return cellDateTimeFormat;
        }
        if (cellType == CellType.CURRENCY_TYPE) {
            return cellCurrencyFormat;
        }
        if (cellType == CellType.NUMERIC_TYPE) {
            if (o instanceof Integer || o instanceof Long) {
                return cellIntegerFormat;
            }
            return cellDecimalFormat;
        }
        return null;
    }


}
