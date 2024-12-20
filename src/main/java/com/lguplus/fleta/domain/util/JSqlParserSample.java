package com.lguplus.fleta.domain.util;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Mar 2022
 */
public class JSqlParserSample {

	public static void main(String[] args) throws JSQLParserException {
		Select stmt = (Select) CCJSqlParserUtil.parse("SELECT col1 AS a, col2 AS b, col3 AS c FROM table WHERE col_1 = 10 AND col_2 = 20 AND col_3 = 30");
		System.out.println("before " + stmt.toString());

		((PlainSelect) stmt.getSelectBody()).getWhere().accept(new ExpressionVisitorAdapter() {
			@Override
			public void visit(Column column) {
				column.setColumnName(column.getColumnName().replace("_", ""));
			}
		});

		System.out.println("after " + stmt.toString());

		String sql = "select * from test_table where a=1 group by c";
		Select select = (Select) CCJSqlParserUtil.parse(sql);
		Expression where = CCJSqlParserUtil.parseCondExpression("a=1 and b=2");
		((PlainSelect) select.getSelectBody()).setWhere(where);
		System.out.println(select.toString());
	}
}
