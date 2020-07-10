package com.kakarote.crm9.utils;

import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.activerecord.generator.Generator;
import com.jfinal.plugin.druid.DruidPlugin;
import com.kakarote.crm9.common.config.JfinalConfig;

import javax.sql.DataSource;

/**
 *
 * 在数据库表有任何变动时，运行一下 main 方法，极速响应变化进行代码重构
 * @author honglei.wan
 */
public class JfinalGenerator {

	public static DataSource getDataSource() {
		DruidPlugin druidPlugin = JfinalConfig.createDruidPlugin();
		druidPlugin.start();
		return druidPlugin.getDataSource();
	}

	public static void main(String[] args) {
		generate();
	}

	public static void generate() {
		String moduleName = "crm";
		// base model 所使用的包名
		String baseModelPackageName = "com.kakarote.crm9.erp."+moduleName+".entity.base";
		// base model 文件保存路径
		String baseModelOutputDir = PathKit.getWebRootPath() + "/generator/base";

		// model 所使用的包名 (MappingKit 默认使用的包名)
		String modelPackageName = "com.kakarote.crm9.erp."+moduleName+".entity";
		// model 文件保存路径 (MappingKit 与 DataDictionary 文件默认保存路径)
		String modelOutputDir = baseModelOutputDir + "/..";

		// 创建生成器
		Generator generator = new Generator(getDataSource(), baseModelPackageName, baseModelOutputDir, modelPackageName, modelOutputDir);
		// 附加注释
		generator.setGenerateRemarks(true);
		// 设置数据库方言
		generator.setDialect(new MysqlDialect());
		// 设置是否生成链式 setter 方法
		generator.setGenerateChainSetter(true);

		// 设置是否在 Model 中生成 dao 对象
		generator.setGenerateDaoInModel(true);
		// 设置是否生成字典文件
		generator.setGenerateDataDictionary(false);
		// 设置需要被移除的表名前缀用于生成modelName。例如表名 "osc_user"，移除前缀 "osc_"后生成的model名为 "User"而非 OscUser
		generator.setRemovedTableNamePrefixes("72crm_");

		generator.setBaseModelTemplate("/config/crm_base_model_template.jf");
		generator.setModelTemplate("/config/crm_model_template.jf");

		// 生成
		generator.generate();
	}
}




