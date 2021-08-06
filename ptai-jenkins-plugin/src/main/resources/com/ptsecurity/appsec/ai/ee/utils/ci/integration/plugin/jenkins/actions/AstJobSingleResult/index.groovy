package com.ptsecurity.appsec.ai.ee.utils.ci.integration.plugin.jenkins.actions.AstJobSingleResult

import com.ptsecurity.appsec.ai.ee.scan.result.ScanBrief
import com.ptsecurity.appsec.ai.ee.scan.result.issue.types.BaseIssue
import com.ptsecurity.appsec.ai.ee.scan.result.issue.types.VulnerabilityIssue
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.Resources
import lib.FormTagLib
import lib.LayoutTagLib

import java.time.Duration
import org.apache.commons.lang3.time.DurationFormatUtils

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

def f = namespace(FormTagLib)
def l = namespace(LayoutTagLib)
def t = namespace('/lib/hudson')
def st = namespace("jelly:stapler")

l.layout(title: "PT AI AST report") {
    l.side_panel() {
        st.include(page: "sidepanel.jelly", from: my.run, it: my.run, optional: true)
    }

    l.main_panel() {
        def scanBriefDetailed = my.getScanBriefDetailed()

        h1(_("result.title"))
        h2(_("scan.settings.title"))
        table(id: "${my.urlName}-settings",
                style: "width: 95%; margin: 0 auto; min-width: 200px", bgcolor: "#ECECEC") {
            colgroup() {
                col(width: "300px")
            }
            tbody() {
                tr() {
                    td(align: "left", style: "padding-left: 20px; padding-top: 8px") {
                        text(_("scan.settings.project"))
                    }
                    td(align: "left", style: "font-weight:bold; padding-top: 8px") {
                        text("${scanBriefDetailed.projectName}")
                    }
                }
                tr() {
                    td(align: "left", style: "padding-left: 20px") {
                        text(_("scan.settings.url"))
                    }
                    td(align: "left", style: "font-weight:bold") {
                        text("${scanBriefDetailed.scanSettings.url}")
                    }
                }
                tr() {
                    td(align: "left", style: "padding-left: 20px") {
                        text(_("scan.settings.language"))
                    }
                    td(align: "left", style: "font-weight:bold") {
                        text("${scanBriefDetailed.scanSettings.language}")
                    }
                }
                tr() {
                    td(align: "left", style: "padding-left: 20px; padding-top: 8px") {
                        text(_("scan.timestamp"))
                    }
                    ZonedDateTime scanDate = ZonedDateTime.parse(scanBriefDetailed.statistic.scanDateIso8601)
                    scanDate = scanDate.withZoneSameInstant(ZoneId.systemDefault())
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss");
                    td(align: "left", style: "font-weight:bold; padding-top: 8px") {
                        text("${scanDate.format(formatter)}")
                    }
                }
                tr() {
                    td(align: "left", style: "padding-left: 20px") {
                        text(_("scan.duration"))
                    }
                    durationMs = Duration.parse(scanBriefDetailed.statistic.scanDurationIso8601).toMillis()
                    td(align: "left", style: "font-weight:bold") {
                        text("${DurationFormatUtils.formatDuration(durationMs, "H:mm:ss", true);}")
                    }
                }
                tr() {
                    td(align: "left", style: "padding-left: 20px; padding-top: 8px") {
                        text(_("environment.server.version"))
                    }
                    td(align: "left", style: "font-weight:bold; padding-top: 8px") {
                        text("${scanBriefDetailed.ptaiServerVersion}")
                    }
                }
                tr() {
                    td(align: "left", style: "padding-left: 20px") {
                        text(_("environment.agent.version"))
                    }
                    td(align: "left", style: "font-weight:bold") {
                        text("${scanBriefDetailed.ptaiAgentVersion}")
                    }
                }
            }
        }
        if (!my.isEmpty()) {
            h2(_("result.breakdown.title"))
            h3(_("result.breakdown.level.title"))
            div(
                    id: "${my.urlName}-level-chart",
                    class: 'graph-cursor-pointer') {}
            table(style: "width: 95%; margin: 0 auto; min-width: 200px", bgcolor: "#FFFFFF") {
                colgroup() {
                    col(width: "50%")
                    col(width: "50%")
                }
                tbody() {
                    tr() {
                        td() {
                            h3(_("result.breakdown.class.title"))
                            div(
                                    id: "${my.urlName}-class-pie-chart",
                                    class: 'graph-cursor-pointer') {}
                        }
                        td() {
                            h3(_("result.breakdown.approvalstate.title"))
                            div(
                                    id: "${my.urlName}-approval-state-pie-chart",
                                    class: 'graph-cursor-pointer') {}
                        }
                    }
                    tr() {
                        td() {
                            h3(_("result.breakdown.suspected.title"))
                            div(
                                    id: "${my.urlName}-suspected-state-pie-chart",
                                    class: 'graph-cursor-pointer') {}                        }
                        td() {
                            h3(_("result.breakdown.scanmode.title"))
                            div(
                                    id: "${my.urlName}-scan-mode-pie-chart",
                                    class: 'graph-cursor-pointer') {}                        }
                    }
                }
            }
            h3(_("result.breakdown.type.title"))
            div(
                    id: "${my.urlName}-type-chart",
                    class: 'graph-cursor-pointer') {}

            script(src: "${rootURL}/plugin/ptai-jenkins-plugin/webjars/echarts/echarts.min.js")
            script(src: "${rootURL}/plugin/ptai-jenkins-plugin/js/charts.js")

            st.bind(var: "action", value: my)
            script """
                var ${my.urlName}Action = action;
    
                // Map vulnerability level to its localized title
                var levelAttrs = {
                    ${BaseIssue.Level.HIGH.name()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_severity_high()}'
                    },
                    ${BaseIssue.Level.MEDIUM.name()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_severity_medium()}'
                    },
                    ${BaseIssue.Level.LOW.name()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_severity_low()}' 
                    },
                    ${BaseIssue.Level.POTENTIAL.name()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_severity_potential()}' 
                    },
                    ${BaseIssue.Level.NONE.name()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_severity_none()}' 
                    }
                };
    
                // Map vulnerability class to its localized title
                var classAttrs = {
                    ${BaseIssue.Type.BLACKBOX.name()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_clazz_blackbox()}'
                    },
                    ${BaseIssue.Type.CONFIGURATION.name()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_clazz_configuration()}'
                    },
                    ${BaseIssue.Type.SCA.name()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_clazz_sca()}'
                    },
                    ${BaseIssue.Type.UNKNOWN.name()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_clazz_unknown()}'
                    },
                    ${BaseIssue.Type.VULNERABILITY.name()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_clazz_vulnerability()}'
                    },
                    ${BaseIssue.Type.WEAKNESS.name()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_clazz_weakness()}'
                    },
                    ${BaseIssue.Type.YARAMATCH.name()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_clazz_yaramatch()}'
                    }
                };
    
                // Map vulnerability class to its localized title
                var approvalStateAttrs = {
                    ${BaseIssue.ApprovalState.NONE.name()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_approval_none()}'
                    },
                    ${BaseIssue.ApprovalState.APPROVAL.name()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_approval_confirmed()}'
                    },
                    ${BaseIssue.ApprovalState.AUTO_APPROVAL.name()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_approval_auto()}'
                    },
                    ${BaseIssue.ApprovalState.DISCARD.name()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_approval_rejected()}'
                    },
                    ${BaseIssue.ApprovalState.NOT_EXIST.name()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_approval_missing()}'
                    }
                };
    
                // Map vulnerability suspected state to its localized title
                var suspectedStateAttrs = {
                    ${true.toString()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_suspected_true()}'
                    },
                    ${false.toString()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_suspected_false()}'
                    }
                };
    
                // Map scan mode to its localized title
                var scanModeAttrs = {
                    ${VulnerabilityIssue.ScanMode.NONE.name()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_scanmode_none()}'
                    },
                    ${VulnerabilityIssue.ScanMode.FROM_ENTRYPOINT.name()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_scanmode_entrypoint()}'
                    },
                    ${VulnerabilityIssue.ScanMode.FROM_OTHER.name()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_scanmode_other()}'
                    },
                    ${VulnerabilityIssue.ScanMode.FROM_PUBLICPROTECTED.name()}: {
                        title: '${Resources.i18n_misc_enums_vulnerability_scanmode_publicprotected()}'
                    }
                };
    
                var maxTypeWidth = 20
                const barHeight = 25
                const bottomMargin = 20
                const axisLabelMargin = 8
                const axisFontFamily = "verdana"
                const axisFontSize = "12px"
                const style = "width: 95%; margin: 0 auto; min-width: 200px; "

                ${my.urlName}Action.getVulnerabilityTypeDistribution(function (response) {
                    var option = response.responseJSON;
                    var dataSet = [];
                    option.tooltip = { trigger: 'axis', axisPointer: { type: 'shadow' } };
                    option.xAxis[0].type = 'value';
                    option.xAxis[0].minInterval = 1;
                    option.yAxis[0].type = 'category';
                    option.series[0].type = 'bar';
                    option.series[0].name = '${_("result.misc.quantity.title")}';
                    // TODO: Use level chart label widths also
                    maxTypeWidth = maxTextWidth(option.yAxis[0].data, axisFontSize + " " + axisFontFamily) + axisLabelMargin;
                    option.grid = { left: maxTypeWidth + "px", top: "0px", bottom: bottomMargin + "px" };
                    divHeight = option.yAxis[0].data.length * barHeight + bottomMargin;
                    \$("${my.urlName}-type-chart").setAttribute("style", style + "height: " + divHeight + "px");                    
                    renderChart("${my.urlName}-type-chart", option);
                     
                    ${my.urlName}Action.getVulnerabilityLevelDistribution(function (response) {
                        var option = response.responseJSON;
                        option.tooltip = { trigger: 'axis', axisPointer: { type: 'shadow' } };
                        option.xAxis[0].type = 'value';
                        option.xAxis[0].minInterval = 1;
                        option.yAxis[0].type = 'category';
                        option.yAxis[0].inverse = false;
                        // replace vulnerability level title values with localized captions
                        option.yAxis[0].data.forEach(function (item, index) {
                            option.yAxis[0].data[index] = levelAttrs[item].title
                        }, option.yAxis[0].data);

                        option.series[0].type = 'bar';
                        option.series[0].name = '${_("result.misc.quantity.title")}';
                        option.grid = { left: maxTypeWidth + "px", top: "0px", bottom: bottomMargin + "px" };
                        divHeight = option.yAxis[0].data.length * barHeight + bottomMargin;
                        \$("${my.urlName}-level-chart").setAttribute("style", style + "height: " + divHeight + "px");
                        renderChart("${my.urlName}-level-chart", option);
                    });  
                    
                    /*
                    ${my.urlName}Action.getVulnerabilitySunBurstB(function (response) {
                        var option = response.responseJSON;
                        option.series[0].type = 'sunburst';
                        option.series[0].radius = [0, '90%'];
                        option.series[0].label = { rotate: 'radial' }
                        // option.grid = { left: maxTypeWidth + "px", top: "0px", bottom: bottomMargin + "px" };
                        divHeight = 200;
                        \$("${my.urlName}-misc-chart").setAttribute("style", style + "height: " + divHeight + "px");
                        renderChart("${my.urlName}-misc-chart", option);
                    });  
                    */
                    
                    ${my.urlName}Action.getVulnerabilityClassPie(function (response) {
                        var option = response.responseJSON;
                        option.tooltip = { trigger: 'item' };
                        option.series[0].itemStyle = {
                            borderRadius: 3,
                            borderColor: '#fff',
                            borderWidth: 2
                        };
                        option.series[0].type = 'pie';
                        option.series[0].radius = ['40%', '70%'];
                        option.series[0].label = { show: false };
                        option.series[0].avoidLabelOverlap = true;
                        
                        option.series[0].data.forEach(function (item, index) {
                            option.series[0].data[index].name = classAttrs[item.name].title
                        }, option.series[0].data);
                        // option.series[0].label = { rotate: 'radial' };
                        option.legend = {
                            orient: 'vertical',
                            left: 'left',
                        };
                        // option.grid = { left: maxTypeWidth + "px", top: "0px", bottom: bottomMargin + "px" };
                        divHeight = 200;
                        \$("${my.urlName}-class-pie-chart").setAttribute("style", style + "height: " + divHeight + "px");
                        renderChart("${my.urlName}-class-pie-chart", option);
                    });  
                    
                    ${my.urlName}Action.getVulnerabilityApprovalStatePie(function (response) {
                        var option = response.responseJSON;
                        option.tooltip = { trigger: 'item' };
                        option.series[0].itemStyle = {
                            borderRadius: 3,
                            borderColor: '#fff',
                            borderWidth: 2
                        };
                        option.series[0].type = 'pie';
                        option.series[0].radius = ['40%', '70%'];
                        option.series[0].label = { show: false };
                        option.series[0].data.forEach(function (item, index) {
                            option.series[0].data[index].name = approvalStateAttrs[item.name].title
                        }, option.series[0].data);
                        // option.series[0].label = { rotate: 'radial' };
                        option.legend = {
                            orient: 'vertical',
                            left: 'left',
                        };
                        // option.grid = { left: maxTypeWidth + "px", top: "0px", bottom: bottomMargin + "px" };
                        divHeight = 200;
                        \$("${my.urlName}-approval-state-pie-chart").setAttribute("style", style + "height: " + divHeight + "px");
                        renderChart("${my.urlName}-approval-state-pie-chart", option);
                    });  
                    
                    ${my.urlName}Action.getVulnerabilitySuspectedPie(function (response) {
                        var option = response.responseJSON;
                        option.tooltip = { trigger: 'item' };
                        option.series[0].itemStyle = {
                            borderRadius: 3,
                            borderColor: '#fff',
                            borderWidth: 2
                        };
                        option.series[0].type = 'pie';
                        option.series[0].radius = ['40%', '70%'];
                        option.series[0].label = { show: false };
                        option.series[0].data.forEach(function (item, index) {
                            option.series[0].data[index].name = suspectedStateAttrs[item.name].title
                        }, option.series[0].data);
                        // option.series[0].label = { rotate: 'radial' };
                        option.legend = {
                            orient: 'vertical',
                            left: 'left',
                        };
                        // option.grid = { left: maxTypeWidth + "px", top: "0px", bottom: bottomMargin + "px" };
                        divHeight = 200;
                        \$("${my.urlName}-suspected-state-pie-chart").setAttribute("style", style + "height: " + divHeight + "px");
                        renderChart("${my.urlName}-suspected-state-pie-chart", option);
                    });  

                    ${my.urlName}Action.getVulnerabilityScanModePie(function (response) {
                        var option = response.responseJSON;
                        option.tooltip = { trigger: 'item' };
                        option.series[0].itemStyle = {
                            borderRadius: 3,
                            borderColor: '#fff',
                            borderWidth: 2
                        };
                        option.series[0].type = 'pie';
                        option.series[0].radius = ['40%', '70%'];
                        option.series[0].label = { show: false };
                        option.series[0].data.forEach(function (item, index) {
                            option.series[0].data[index].name = scanModeAttrs[item.name].title
                        }, option.series[0].data);
                        // option.series[0].label = { rotate: 'radial' };
                        option.legend = {
                            orient: 'vertical',
                            left: 'left',
                        };
                        // option.grid = { left: maxTypeWidth + "px", top: "0px", bottom: bottomMargin + "px" };
                        divHeight = 200;
                        \$("${my.urlName}-scan-mode-pie-chart").setAttribute("style", style + "height: " + divHeight + "px");
                        renderChart("${my.urlName}-scan-mode-pie-chart", option);
                    });  

                });
            """
        } else {

        }
    }
}
