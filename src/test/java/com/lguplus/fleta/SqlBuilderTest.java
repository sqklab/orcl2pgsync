package com.lguplus.fleta;

import com.lguplus.fleta.domain.util.SQLBuilder;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SqlBuilderTest {


	@Test
	void test2() {
		String query = "delete from imcsuser.rd_vl_ab_album_image where test_id = :p_test_id and screen_type = case when :p_img_flag   --pk1:p_screen_type";
		String list = SQLBuilder.changeParameterToQuestionMark(query);


		System.out.println("new query = " + list);
	}
}
