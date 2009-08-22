package org.netspective.io.spreadsheet.model;

import org.apache.poi.ss.usermodel.Cell;
import org.netspective.io.spreadsheet.template.Column;

public interface TableCell
{
    public final static boolean ALLOW_EMPTY_STRING = true;
    public final static boolean EMPTY_STRING_NOT_ALLOWED = false;

    public Cell getCell();
    public boolean isBlankCell();

    public Column getColumn();

    public Object getValue();
    public Object getValue(final Object defaultIfBlank);
    public Object getValue(final Object defaultIfBlank, final Object defaultIfError);
}
