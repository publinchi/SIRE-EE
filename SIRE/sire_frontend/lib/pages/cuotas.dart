import 'dart:convert';

import 'dart:math' as math;

import 'package:animations/animations.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:intl/intl.dart';
import 'package:sire_frontend/charts/pie_chart.dart';
import 'package:sire_frontend/charts/vertical_fraction_bar.dart';
import 'package:sire_frontend/colors.dart';
import 'package:sire_frontend/data.dart';
import 'package:sire_frontend/data/options.dart';
import 'package:sire_frontend/formatters.dart';
import 'package:sire_frontend/layout/adaptive.dart';

import 'package:sire_frontend/layout/text_scale.dart';

import 'package:flutter_gen/gen_l10n/gallery_localizations.dart';
import 'package:sire_frontend/main.dart';
import 'package:sire_frontend/pages/camara.dart';

class CuotasView extends StatelessWidget {

  const CuotasView({
    this.heroLabel,
    this.heroAmount,
    this.wholeAmount,
    this.segments,
    this.financialEntityCards,
  }) : assert(segments.length == financialEntityCards.length);

  /// The amounts to assign each item.
  final List<RallyPieChartSegment> segments;
  final String heroLabel;
  final double heroAmount;
  final double wholeAmount;
  final List<EntityCuotasView> financialEntityCards;

  @override
  Widget build(BuildContext context) {
    final maxWidth = pieChartMaxSize + (cappedTextScale(context) - 1.0) * 100.0;
    return LayoutBuilder(builder: (context, constraints) {
      return Column(
        children: [
          ConstrainedBox(
            constraints: BoxConstraints(
              // We decrease the max height to ensure the [RallyPieChart] does
              // not take up the full height when it is smaller than
              // [kPieChartMaxSize].
              maxHeight: math.min(
                constraints.biggest.shortestSide * 0.9,
                maxWidth,
              ),
            ),
            child: RallyPieChart(
              heroLabel: heroLabel,
              heroAmount: heroAmount,
              wholeAmount: wholeAmount,
              segments: segments,
            ),
          ),
          const SizedBox(height: 24),
          Container(
            height: 1,
            constraints: BoxConstraints(maxWidth: maxWidth),
            color: RallyColors.inputBackground,
          ),
          Container(
            constraints: BoxConstraints(maxWidth: maxWidth),
            color: RallyColors.cardBackground,
            child: Column(
              children: financialEntityCards,
            ),
          ),
        ],
      );
    });
  }
}

class EntityCuotasView extends StatelessWidget {
  const EntityCuotasView({
    @required this.indicatorColor,
    @required this.indicatorFraction,
    @required this.title,
    @required this.subtitle,
    @required this.semanticsLabel,
    @required this.amount,
    @required this.suffix,
    @required this.cuotas,
  });

  final Color indicatorColor;
  final double indicatorFraction;
  final String title;
  final String subtitle;
  final String semanticsLabel;
  final String amount;
  final Widget suffix;
  final Future<List<DetailedCuotaData>> cuotas;

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    return Semantics.fromProperties(
      properties: SemanticsProperties(
        button: true,
        enabled: true,
        label: semanticsLabel,
      ),
      excludeSemantics: true,
      // TODO(shihaohong): State restoration of
      // FinancialEntityCategoryDetailsPage on mobile is blocked because
      // OpenContainer does not support restorablePush.
      // See https://github.com/flutter/flutter/issues/69924.
      child: OpenContainer(
        transitionDuration: const Duration(milliseconds: 350),
        transitionType: ContainerTransitionType.fade,
        openBuilder: (context, openContainer) =>
            EntityCuotasDetailsPage(
              numContrato: int.parse(title),
              cuotas: cuotas,
            ),
        openColor: RallyColors.primaryBackground,
        closedColor: RallyColors.primaryBackground,
        closedElevation: 0,
        closedBuilder: (context, openContainer) {
          return TextButton(
            style: TextButton.styleFrom(primary: Colors.black),
            onPressed: openContainer,
            child: Column(
              children: [
                Container(
                  padding:
                  const EdgeInsets.symmetric(vertical: 16, horizontal: 8),
                  child: Row(
                    children: [
                      Container(
                        alignment: Alignment.center,
                        height: 32 + 60 * (cappedTextScale(context) - 1),
                        padding: const EdgeInsets.symmetric(horizontal: 12),
                        child: VerticalFractionBar(
                          color: indicatorColor,
                          fraction: indicatorFraction,
                        ),
                      ),
                      Expanded(
                        child: Wrap(
                          alignment: WrapAlignment.spaceBetween,
                          crossAxisAlignment: WrapCrossAlignment.center,
                          children: [
                            Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  title,
                                  style: textTheme.bodyText2
                                      .copyWith(fontSize: 16),
                                ),
                                Text(
                                  subtitle,
                                  style: textTheme.bodyText2
                                      .copyWith(color: RallyColors.gray60),
                                ),
                              ],
                            ),
                            Text(
                              amount,
                              style: textTheme.bodyText1.copyWith(
                                fontSize: 20,
                                color: RallyColors.gray,
                              ),
                            ),
                          ],
                        ),
                      ),
                      Container(
                        constraints: const BoxConstraints(minWidth: 32),
                        padding: const EdgeInsetsDirectional.only(start: 12),
                        child: suffix,
                      ),
                    ],
                  ),
                ),
                const Divider(
                  height: 1,
                  indent: 16,
                  endIndent: 16,
                  color: RallyColors.dividerColor,
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}

EntityCuotasView buildFinancialEntityFromContratoData(
    ContratoData model,
    int accountDataIndex,
    BuildContext context,
    Future<List<DetailedCuotaData>> cuotas,
    ) {
  final amount = usdWithSignFormat(context).format(model.valorContrato);
  final formatter = DateFormat("dd/MM/yyyy");
  final fecha = formatter.format(model.fechaContrato);

  return EntityCuotasView(
    suffix: const Icon(Icons.chevron_right, color: Colors.grey),
    title: model.numContrato.toString(),
    subtitle: '$fecha',
    semanticsLabel: GalleryLocalizations.of(context).rallyAccountAmount(
      model.numContrato,
      fecha,
      amount,
    ),
    indicatorColor: RallyColors.accountColor(accountDataIndex),
    indicatorFraction: 1,
    amount: "Consultar Cuotas",
    cuotas: cuotas,
  );
}

List<EntityCuotasView> buildContratoDataListViews(
    List<ContratoData> items,
    BuildContext context,
    Future<List<DetailedCuotaData>> cuotas,
    ) {
  return List<EntityCuotasView>.generate(
    items.length,
        (i) => buildFinancialEntityFromContratoData(
        items[i],
        i,
        context,
        cuotas
    ),
  );
}

class EntityCuotasDetailsPage extends StatelessWidget {

  EntityCuotasDetailsPage({this.numContrato, this.cuotas});

  final List<DetailedEventData> items =
  DummyDataService.getDetailedEventItems();
  final int numContrato;
  final Future<List<DetailedCuotaData>> cuotas;

  @override
  Widget build(BuildContext context) {
    final isDesktop = isDisplayDesktop(context);

    return ApplyTextOptions(
        child: FutureBuilder<List<DetailedCuotaData>>(
            future: cuotas,
            builder: (context, snapshot) {
              if (!snapshot.hasData) {
                return const Center(child: CircularProgressIndicator());
              }
              return Scaffold(
                appBar: AppBar(
                  elevation: 0,
                  centerTitle: true,
                  title: Text(
                    "Cuotas", //TODO
                    style: Theme.of(context)
                        .textTheme
                        .bodyText2
                        .copyWith(fontSize: 18),
                  ),
                ),
                body: Column(
                  children: [
                    /*SizedBox(
                      height: 200,
                      width: double.infinity,
                      child: RallyLineChart(events: items),
                    ),*/
                    Expanded(
                      child: Padding(
                        padding: isDesktop
                            ? const EdgeInsets.all(40)
                            : EdgeInsets.zero,
                        child: ListView(
                          shrinkWrap: true,
                          children: [
                            for (DetailedCuotaData detailedEventData
                            in snapshot.data)
                              _DetailedCuotasCard(
                                fechaCuota: detailedEventData.fechaCuota,
                                valorCuota: detailedEventData.valorCuota,
                                actualizoPor: detailedEventData.actualizoPor,
                                codCliente: detailedEventData.codCliente,
                                numContrato: numContrato,
                                nroCuota: detailedEventData.nroCuota,
                                estadoCuota: detailedEventData.estado_cuota,
                              ),
                          ],
                        ),
                      ),
                    ),
                  ],
                ),
              );
            }
        )
    );
  }
}

class _DetailedCuotasCard extends StatelessWidget {

  const _DetailedCuotasCard({
    @required this.codCliente,
    @required this.numContrato,
    @required this.nroCuota,
    @required this.fechaCuota,
    @required this.valorCuota,
    @required this.estadoCuota,
    @required this.actualizoPor,
  });

  final String codCliente;
  final int numContrato;
  final int nroCuota;
  final DateTime fechaCuota;
  final double valorCuota;
  final String estadoCuota;
  final String actualizoPor;

  @override
  Widget build(BuildContext context) {
    final isDesktop = isDisplayDesktop(context);
    return TextButton(
      style: TextButton.styleFrom(
        primary: Colors.black,
        padding: const EdgeInsets.symmetric(horizontal: 16),
      ),
      onPressed: () => {
        if(estadoCuota == "EN PLANILLA" || estadoCuota == "RECHAZADO")
          Navigator.push(
            context,
            MaterialPageRoute(
                builder: (context) =>
                    TakePictureScreen(
                      camera: firstCamera,
                      idCliente: codCliente,
                      idContrato: numContrato,
                      idCuota: nroCuota,
                      valorCuota: valorCuota,
                    )
            ),
          )
      },
      child: Column(
        children: [
          Container(
            padding: const EdgeInsets.symmetric(vertical: 16),
            width: double.infinity,
            child: isDesktop
                ? Row(
              children: [
                Expanded(
                  flex: 1,
                  child: _EventCuotaTitle(
                    title: nroCuota.toString(),
                    estadoCuota: estadoCuota,
                  ),
                ),
                _EventCuotaDate(date: fechaCuota),
                Expanded(
                  flex: 1,
                  child: Align(
                    alignment: AlignmentDirectional.centerEnd,
                    child: _EventCuotaAmount(
                      amount: valorCuota,
                      estadoCuota: estadoCuota,
                    ),
                  ),
                ),
              ],
            )
                : Wrap(
              alignment: WrapAlignment.spaceBetween,
              children: [
                Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _EventCuotaTitle(
                      title: nroCuota.toString(),
                      estadoCuota: estadoCuota,
                    ),
                    _EventCuotaDate(date: fechaCuota),
                  ],
                ),
                _EventCuotaAmount(
                  amount: valorCuota,
                  estadoCuota: estadoCuota,
                ),
              ],
            ),
          ),
          SizedBox(
            height: 1,
            child: Container(
              color: RallyColors.dividerColor,
            ),
          ),
        ],
      ),
    );
  }
}

class _EventCuotaAmount extends StatelessWidget {
  const _EventCuotaAmount({
    Key key,
    @required this.amount,
    @required this.estadoCuota,
  }) : super(key: key);

  final double amount;
  final String estadoCuota;

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    if(estadoCuota == "RECIBIDO")
      return Text(
        usdWithSignFormat(context).format(amount),
        style: textTheme.bodyText1.copyWith(
          fontSize: 20,
          color: Colors.yellow,
        ),
      );
    else if(estadoCuota == "RECHAZADO")
      return Text(
        usdWithSignFormat(context).format(amount),
        style: textTheme.bodyText1.copyWith(
          fontSize: 20,
          color: Colors.red,
        ),
      );
    else if(estadoCuota == "CANCELADO")
      return Text(
        usdWithSignFormat(context).format(amount),
        style: textTheme.bodyText1.copyWith(
          fontSize: 20,
          color: Colors.green,
        ),
      );
    else
      return Text(
        usdWithSignFormat(context).format(amount),
        style: textTheme.bodyText1.copyWith(
          fontSize: 20,
          color: RallyColors.gray,
        ),
      );
  }
}

class _EventCuotaDate extends StatelessWidget {
  const _EventCuotaDate({Key key, @required this.date}) : super(key: key);

  final DateTime date; //TODO

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    return Text(
      shortDateFormat(context).format(date), //TODO
      semanticsLabel: longDateFormat(context).format(date), //TODO
      style: textTheme.bodyText2.copyWith(color: RallyColors.gray60),
    );
  }
}

class _EventCuotaTitle extends StatelessWidget {
  const _EventCuotaTitle({
    Key key,
    @required this.title,
    @required this.estadoCuota,
  }) : super(key: key);

  final String title;
  final String estadoCuota;

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    return Text(
      title + " " + estadoCuota,
      style: textTheme.bodyText2.copyWith(fontSize: 16),
    );
  }
}