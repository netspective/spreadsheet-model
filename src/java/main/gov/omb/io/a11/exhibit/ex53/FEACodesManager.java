package gov.omb.io.a11.exhibit.ex53;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.netspective.io.spreadsheet.consumer.DefaultWorksheetDataHandler;
import org.netspective.io.spreadsheet.consumer.WorksheetConsumer;
import org.netspective.io.spreadsheet.consumer.WorksheetDataHandler;
import org.netspective.io.spreadsheet.message.DefaultMessage;
import org.netspective.io.spreadsheet.message.Message;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.template.Column;
import org.netspective.io.spreadsheet.template.ColumnGroup;
import org.netspective.io.spreadsheet.template.DefaultColumn;
import org.netspective.io.spreadsheet.template.WorksheetTemplate;
import org.netspective.io.spreadsheet.util.Util;
import org.netspective.io.spreadsheet.validate.ValidationContext;
import org.netspective.io.spreadsheet.validate.cell.CellValidationMessage;
import org.netspective.io.spreadsheet.validate.row.RowValidationRule;
import org.netspective.io.spreadsheet.validate.row.ValidateColumnData;
import org.netspective.io.spreadsheet.validate.template.TemplateValidationRule;
import org.netspective.io.spreadsheet.validate.template.ValidateColumnHeadingsRule;
import org.netspective.io.spreadsheet.value.StringValueHandler;
import org.netspective.io.spreadsheet.value.ValueHandler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FEACodesManager
{
    private static final FEACodesManager INSTANCE = new FEACodesManager();

    public static FEACodesManager getInstance()
    {
        return INSTANCE;
    }

    public class FEACodesWorksheetTemplate implements WorksheetTemplate, ValidationContext
    {
        private final Column codeColumn;
        private final Column captionColumn;
        private final List<Column> columns = new ArrayList<Column>();
        private final Map<Integer, Column> columnsMapByIndex = new HashMap<Integer, Column>();
        private final List<TemplateValidationRule> templateValidationRules = new ArrayList<TemplateValidationRule>();
        private final List<RowValidationRule> rowValidationRules = new ArrayList<RowValidationRule>();

        public FEACodesWorksheetTemplate(final int headingsRow, final String codeColumnName, final String captionColumnName)
        {
            final ValueHandler codeValueHandler = new StringValueHandler("%03.0f");  // Excel might see the cells as #'s so we need to format it properly
            final ValueHandler captionValueHandler = new StringValueHandler();

            codeColumn = new DefaultColumn(codeValueHandler, -1, headingsRow, 1, codeColumnName);
            captionColumn = new DefaultColumn(captionValueHandler, -1, headingsRow, 2, captionColumnName);

            columns.add(codeColumn);
            columns.add(captionColumn);

            for(final Column column : columns)
                columnsMapByIndex.put(column.getColumnIndex(), column);
            
            templateValidationRules.add(new ValidateColumnHeadingsRule("INVALID_COL_HEADING"));
            rowValidationRules.add(new ValidateColumnData("INVALID_COLUMN"));
        }

        public Column getCodeColumn()
        {
            return codeColumn;
        }

        public Column getCaptionColumn()
        {
            return captionColumn;
        }

        public List<Column> getColumns()
        {
            return columns;
        }

        public List<ColumnGroup> getColumnGroups()
        {
            return null;
        }

        public boolean hasColumnGroups()
        {
            return false;
        }

        public Map<Integer, Column> getColumnsMapByIndex()
        {
            return Collections.unmodifiableMap(columnsMapByIndex);
        }

        public List<TemplateValidationRule> getTemplateValidationRules()
        {
            return Collections.unmodifiableList(templateValidationRules);
        }

        public List<RowValidationRule> getRowValidationRules()
        {
            return Collections.unmodifiableList(rowValidationRules);
        }

        public ValidationContext createValidationContext()
        {
            return this;
        }

        public boolean isWarning(final Message message)
        {
            return false;
        }

        public String getValidationMessageRowSummary(final TableRow row, final CellValidationMessage[] cellMessages)
        {
            return String.format("Row %d: %d error(s).", row.getRowNumberInSheet(), cellMessages.length);
        }

        public String getValidationMessageCellLocator(final TableCell cell, final boolean useInFormatSpec)
        {
            final String cellLocation = Util.getCellLocator(cell.getCell());
            final String columnName = cell.getColumn().getQualifiedColumnName();
            final String filteredName = useInFormatSpec ? columnName.replace("%", "%%") : columnName;
            return String.format("%s (\"%s\")", cellLocation, filteredName);
        }
    }

    private final List<Message> errors = new ArrayList<Message>();
    private final List<Message> warnings = new ArrayList<Message>();
    private final Map<String, String> lineOfBusinessOrServiceTypesCache = new HashMap<String, String>();
    private final Map<String, String> subFunctionOrSvcComponentsCache = new HashMap<String, String>();

    public FEACodesManager()
    {
        final int headingRowNumber = 3;

        final String workbookResourceInClassPath = "/gov/omb/data/domain/egov/fea/FEA_LOB_Svc_and_Sub_Function_Codes.xls";
        final Workbook workbook;
        try
        {
            final InputStream is = getClass().getResourceAsStream(workbookResourceInClassPath);
            workbook = WorkbookFactory.create(is);
        }
        catch (Exception e)
        {
            errors.add(new DefaultMessage("FILE_NOT_FOUND", "Unable to open FEA codes workbook %s: %s %s.", workbookResourceInClassPath, e.getClass(), e.getMessage()));
            return;
        }

        cacheCodes(workbook, workbookResourceInClassPath, "LOB or Svc Type",
                new FEACodesWorksheetTemplate(headingRowNumber, "Valid Code", "Line of Business or Service Type"),
                lineOfBusinessOrServiceTypesCache);

        cacheCodes(workbook, workbookResourceInClassPath, "Sub-Funct or Svc Comp",
                new FEACodesWorksheetTemplate(headingRowNumber, "Valid Code", "Sub-Function or Svc Component"),
                subFunctionOrSvcComponentsCache);        
    }

    public void cacheCodes(final Workbook workbook, final String workbookResourceInClassPath,
                           final String codesSheetName, final FEACodesWorksheetTemplate template,
                           final Map<String, String> cache)
    {
        Sheet codesSheet = workbook.getSheet(codesSheetName);
        if(codesSheet == null)
        {
            errors.add(new DefaultMessage("SHEET_NOT_FOUND", "Unable to find sheet %s in codes workbook %s.", codesSheetName, workbookResourceInClassPath));
            return;
        }

        final WorksheetDataHandler dataHandler = new DefaultWorksheetDataHandler(3, codesSheet.getLastRowNum(), 1, 2);
        final WorksheetConsumer consumer = new WorksheetConsumer(codesSheet, template, dataHandler, null);
        final WorksheetConsumer.TemplateValidationResult tvr = consumer.validateTemplate();
        if(tvr.isValid())
        {
            final WorksheetConsumer.DataValidationResult dvr = consumer.validateData();
            if(tvr.isValid())
            {
                for(final TableRow row : dvr.getTable().getRows())
                {
                    final TableCell codeColumn = row.findCellForColumn(template.getCodeColumn());
                    final TableCell captionColumn = row.findCellForColumn(template.getCaptionColumn());

                    if(codeColumn == null || captionColumn == null)
                    {
                        warnings.add(new DefaultMessage("INVALID_CODE", "Unable to find code column %s (%s) on row %d in worksheet %s of workbook %s.", template.getCodeColumn().getColumnName(), template.getCaptionColumn().getColumnName(), row.getRowNumberInSheet(), codesSheetName, workbookResourceInClassPath));
                        continue;
                    }

                    final Object code = codeColumn.getValue(null);
                    final Object caption = captionColumn.getValue(null);

                    if(code == null || caption == null || code.toString().trim().length() == 0 || caption.toString().trim().length() == 0)
                    {
                        warnings.add(new DefaultMessage("INVALID_CODE", "Found invalid code %s (%s) on row %d in worksheet %s of workbook %s.", code, caption, row.getRowNumberInSheet(), codesSheetName, workbookResourceInClassPath));
                        continue;
                    }

                    if(cache.get(code.toString()) != null)
                        errors.add(new DefaultMessage("DUPLICATE_CODE", "Found duplicate code %s (%s) in worksheet %s of workbook %s.", code, caption, codesSheetName, workbookResourceInClassPath));
                    else
                        cache.put(code.toString(), caption.toString());
                }
            }
            else
                errors.addAll(Arrays.asList(dvr.getErrors()));
        }
        else
            errors.addAll(Arrays.asList(tvr.getErrors()));
    }

    public boolean isValidLineOfBusinessOrServiceTypeCode(final String code)
    {
        return lineOfBusinessOrServiceTypesCache.get(code) != null;
    }

    public boolean isValidSubfunctionOrSvComponentCode(final String code)
    {
        return subFunctionOrSvcComponentsCache.get(code) != null;
    }

    public Map<String, String> getLineOfBusinessOrServiceTypesCache()
    {
        return Collections.unmodifiableMap(lineOfBusinessOrServiceTypesCache);
    }

    public Map<String, String> getSubFunctionOrSvcComponentsCache()
    {
        return Collections.unmodifiableMap(subFunctionOrSvcComponentsCache);
    }

    public List<Message> getWarnings()
    {
        return warnings;
    }

    public List<Message> getErrors()
    {
        return errors;
    }

    public boolean isValid()
    {
        return errors.size() == 0;
    }
}
