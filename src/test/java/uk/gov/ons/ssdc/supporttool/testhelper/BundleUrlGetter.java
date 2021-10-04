package uk.gov.ons.ssdc.supporttool.testhelper;

@FunctionalInterface
public interface BundleUrlGetter {
  String getUrl(BundleOfUsefulTestStuff bundle);
}
