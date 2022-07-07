package danigpam.propertiestoobject.domain;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sample")
public class SampleConfig {

	private String text;
	private List<String> list;
	private List<SampleConfigDetails> details;
	private Map<String, SampleConfigDetails> map;
	
	public static class SampleConfigDetails {
		
		private String text;
		private Boolean bool;
		private Double decimal;
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public Boolean isBool() {
			return bool;
		}
		public void setBool(Boolean bool) {
			this.bool = bool;
		}
		public Double getDecimal() {
			return decimal;
		}
		public void setDecimal(Double decimal) {
			this.decimal = decimal;
		}
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public List<String> getList() {
		return list;
	}
	public void setList(List<String> list) {
		this.list = list;
	}
	public List<SampleConfigDetails> getDetails() {
		return details;
	}
	public void setDetails(List<SampleConfigDetails> details) {
		this.details = details;
	}
	public Map<String, SampleConfigDetails> getMap() {
		return map;
	}
	public void setMap(Map<String, SampleConfigDetails> map) {
		this.map = map;
	}
}
