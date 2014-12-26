package laf.core.translation.labels;

import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;

import laf.core.translation.TString;
import laf.core.translation.labels.PropertyReflectionUtil.Property;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.inject.Inject;

public class LabelUtil {

	@Inject
	PropertyReflectionUtil reflectionUtil;

	/**
	 * Get the label of a certain property in a type in the default variant
	 */
	public TString getPropertyLabel(Class<?> type, String propertyName) {
		return getPropertyLabel(type, propertyName, "");
	}

	/**
	 * Get the label of a certain property in a type
	 */
	public TString getPropertyLabel(Class<?> type, String propertyName,
			String variant) {
		// find property
		Property property = reflectionUtil.getPropertyIntroduction(type,
				propertyName);
		if (property == null) {
			throw new IllegalArgumentException("No property " + propertyName
					+ " found in " + type);
		}

		// check that propertiesLabeled annotation is present
		{
			PropertiesLabeled propertiesLabeled = property.getDeclaringType()
					.getAnnotation(PropertiesLabeled.class);

			if (propertiesLabeled == null) {
				throw new RuntimeException(
						"No PropertiesLabeled annotation present on " + type
								+ " while retrieving label for property "
								+ propertyName);
			}

			if (!"".equals(variant)
					&& !Arrays.asList(propertiesLabeled.variants()).contains(
							variant)) {
				throw new RuntimeException("Label variant " + variant
						+ " not available for " + type
						+ ". Available variants: <"
						+ Joiner.on(", ").join(propertiesLabeled.variants())
						+ ">");
			}
		}

		// build and return TString
		return new TString(
				property.getDeclaringType().getName() + "." + propertyName
						+ (variant.isEmpty() ? "" : "." + variant),
				calculatePropertyFallback(type, propertyName, variant, property));
	}

	public TString getEnumMemberLabel(Enum<?> member) {
		return getEnumMemberLabel(member, "");
	}

	public TString getEnumMemberLabel(Enum<?> member, String variant) {
		MembersLabeled membersLabeled = member.getDeclaringClass()
				.getAnnotation(MembersLabeled.class);

		// check that MembersLabeled annotation is present
		if (membersLabeled == null) {
			throw new RuntimeException("Missing MembersLabeled annotation for "
					+ member.getDeclaringClass()
					+ " while retrieving label for " + member);
		}

		// check that variant is allowed
		if (!"".equals(variant)
				&& !Arrays.asList(membersLabeled.variants()).contains(variant)) {
			throw new RuntimeException("Label variant " + variant
					+ " not available for member " + member + " of "
					+ member.getDeclaringClass() + ". Available variants: <"
					+ Joiner.on(", ").join(membersLabeled.variants()) + ">");
		}

		return new TString(member.getDeclaringClass().getName() + "."
				+ member.name() + (variant.isEmpty() ? "" : "." + variant),
				calculateEnumMemberFallback(member, variant));
	}

	protected String calculateEnumMemberFallback(Enum<?> member, String variant) {
		Field memberField;
		try {
			memberField = member.getDeclaringClass().getField(member.name());
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
		List<String> labels = extractLabels(memberField, variant);
		if (labels.size() > 1) {
			throw new RuntimeException(
					"Multiple Label annotations found for member " + member
							+ " of " + member.getDeclaringClass()
							+ " using variant " + variant);
		}
		if (labels.size() == 1) {
			return labels.get(0);
		}

		return insertSpacesIntoCamelCaseString(CaseFormat.UPPER_UNDERSCORE.to(
				CaseFormat.UPPER_CAMEL, member.name()));
	}

	protected List<String> extractLabels(AnnotatedElement annotated,
			String variant) {
		return Stream.concat(
				Arrays.stream(annotated.getAnnotationsByType(Label.class))
						.filter(l -> variant.equals(l.variant()))
						.map(l -> l.value()),
				extractLabelsOfVariantAnnotations(annotated, variant)).collect(
				toList());
	}

	public TString getTypeLabel(Class<?> type) {
		return getTypeLabel(type, "");
	}

	/**
	 * Return the label variants available for a type
	 */
	public Set<String> availableTypeLabelVariants(Class<?> type) {
		Set<String> result = new HashSet<String>();
		result.add("");

		// add variants from Labeled
		Arrays.stream(type.getAnnotation(Labeled.class).variants()).forEach(
				result::add);

		// add variants from Label
		Arrays.stream(type.getAnnotationsByType(Label.class))
				.map(Label::variant).forEach(result::add);

		// add variants from variant annotations
		for (Annotation a : type.getAnnotations()) {
			LabelVariant labelVariant = a.annotationType().getAnnotation(
					LabelVariant.class);
			if (labelVariant != null) {
				result.add(labelVariant.value());
			}
		}

		return result;
	}

	public TString getTypeLabel(Class<?> type, String variant) {
		Labeled labeled = type.getAnnotation(Labeled.class);
		if (labeled == null) {
			throw new RuntimeException("Missing Labeled annotation on " + type);
		}

		Set<String> availableVariants = availableTypeLabelVariants(type);
		if (!availableVariants.contains(variant)) {
			throw new RuntimeException("Label variant " + variant
					+ " not available for " + type + ". Available variants: <"
					+ Joiner.on(", ").join(availableVariants) + ">");
		}

		return new TString(type.getName()
				+ (variant.isEmpty() ? "" : "." + variant),
				calculateTypeFallback(type, variant));
	}

	public static Stream<String> extractLabelsOfVariantAnnotations(
			AnnotatedElement annotated, String variant) {
		return Arrays
				.stream(annotated.getAnnotations())
				.filter(a -> {
					LabelVariant labelVariant = a.annotationType()
							.getAnnotation(LabelVariant.class);

					return labelVariant != null
							&& variant.equals(labelVariant.value());
				})
				.map(a -> {
					try {
						return (String) a.annotationType().getMethod("value")
								.invoke(a);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
	}

	protected String calculateTypeFallback(Class<?> type, String variant) {
		List<String> annotations = extractLabels(type, variant);
		if (annotations.isEmpty()) {
			return insertSpacesIntoCamelCaseString(type.getSimpleName());
		}

		if (annotations.size() == 1) {
			return annotations.get(0);
		}

		throw new RuntimeException("Multiple Label annotations found for type "
				+ type + " and variant " + variant);
	}

	/**
	 * Calculate the fallback string for a property. Override to customize
	 */
	protected String calculatePropertyFallback(Class<?> type, String name,
			String variant, Property property) {

		ArrayList<String> labels = new ArrayList<>();
		ArrayList<String> labelLocations = new ArrayList<>();

		processLabelAnnotations(property.getGetter(), "getter", labels,
				labelLocations, variant);
		processLabelAnnotations(property.getSetter(), "setter", labels,
				labelLocations, variant);
		processLabelAnnotations(property.getBackingField(), "backingField",
				labels, labelLocations, variant);

		if (labels.size() == 0) {
			return calculateFallbackFromPropertyName(name);
		} else if (labels.size() == 1) {
			return labels.get(0);
		} else {
			throw new RuntimeException("Label annotations found on "
					+ Joiner.on(",").join(labelLocations) + " for property "
					+ property);
		}
	}

	private void processLabelAnnotations(AnnotatedElement annotated,
			String location, ArrayList<String> labels,
			ArrayList<String> labelLocations, String variant) {
		if (annotated != null) {
			List<String> foundLabels = extractLabels(annotated, variant);
			if (foundLabels.size() > 1) {
				throw new RuntimeException(
						"More than one Label annotation found on " + annotated
								+ " for variant " + variant);
			} else if (foundLabels.size() == 1) {
				labelLocations.add(location);
				labels.add(foundLabels.get(0));
			}
		}
	}

	/**
	 * Calculate the fallback string for a property from it's name. Override to
	 * customize
	 */
	protected String calculateFallbackFromPropertyName(String name) {
		return insertSpacesIntoCamelCaseString(CaseFormat.LOWER_CAMEL.to(
				CaseFormat.UPPER_CAMEL, name));
	}

	public static String insertSpacesIntoCamelCaseString(String str) {
		StringBuilder sb = new StringBuilder(str.length());
		boolean isFirst = true;
		for (int c : str.codePoints().toArray()) {
			if (!isFirst && Character.isUpperCase(c)) {
				sb.append(" ");
			}
			isFirst = false;
			sb.appendCodePoint(c);
		}
		return sb.toString();
	}
}
